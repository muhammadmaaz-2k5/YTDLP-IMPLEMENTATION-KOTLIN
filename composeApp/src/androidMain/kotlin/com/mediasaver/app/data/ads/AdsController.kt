package com.mediasaver.app.data.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Standard ad formats supported across the app.
 */
enum class AdFormat {
    BANNER,
    NATIVE,
    INTERSTITIAL,
    REWARDED,
    REWARDED_INTERSTITIAL,
    APP_OPEN
}

/**
 * Reactive events emitted during ad lifecycle for telemetry, debugging, and UI responsiveness.
 */
sealed interface AdEvent {
    data class StateChanged(val adsEnabled: Boolean) : AdEvent
    data class AdLoaded(val format: AdFormat) : AdEvent
    data class AdFailedToLoad(val format: AdFormat, val errorMessage: String, val errorCode: Int = -1) : AdEvent
    data class AdShown(val format: AdFormat) : AdEvent
    data class AdDismissed(val format: AdFormat) : AdEvent
    data class AdClicked(val format: AdFormat) : AdEvent
    data class RewardEarned(val format: AdFormat, val amount: Int = 1, val type: String = "Reward") : AdEvent
}

/**
 * Every AdMob/UMP SDK touchpoint in the app lives here — `androidApp` only makes thin,
 * plain-Kotlin calls into this object (`initializeConsentAndAds`, `preloadAll`, etc.), the same
 * "SDK-specific code stays in composeApp/androidMain" pattern already used for the download
 * engine ([com.mediasaver.app.data.download]). commonMain screens never see AdMob types at
 * all — they call `expect` functions/composables (see `PlatformUtils.kt` and `AdSlots.kt`).
 *
 * Consent (UMP) is not optional polish: showing ads to EEA/UK/California users without asking
 * first is a legal requirement, not just an AdMob policy — see [initializeConsentAndAds].
 */
object AdsController {

    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    private val _events = MutableSharedFlow<AdEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AdEvent> = _events.asSharedFlow()

    private val eventListeners = CopyOnWriteArrayList<(AdEvent) -> Unit>()

    fun addEventListener(listener: (AdEvent) -> Unit): () -> Unit {
        eventListeners.add(listener)
        return { eventListeners.remove(listener) }
    }

    fun notifyEvent(event: AdEvent) {
        _events.tryEmit(event)
        eventListeners.forEach { runCatching { it(event) } }
    }

    fun isAdsEnabled(): Boolean = _adsEnabled.value

    fun setAdsEnabled(enabled: Boolean) {
        if (_adsEnabled.value == enabled) return
        _adsEnabled.value = enabled
        notifyEvent(AdEvent.StateChanged(enabled))
        if (!enabled) {
            interstitialAd = null
            rewardedAd = null
            rewardedInterstitialAd = null
            appOpenAdManager.clearAd()
            retryHandler.removeCallbacksAndMessages(null)
        } else {
            appContextForRetry?.let { preloadAll(it) }
        }
    }

    private var consentInformation: ConsentInformation? = null
    private var adsInitialized = false

    // Set once real ad flow is allowed to start (after onboarding, not during the first cold
    // launch) — see [enableAppOpenAds]. Prevents an App Open ad interrupting first-run setup.
    private var appOpenAdsEnabled = false

    private var currentActivityRef: WeakReference<Activity>? = null

    // Held only to resume preloading after a transient failure (see [retryWithBackoff]) — never
    // used to request ads outside the normal preload/show flow.
    private var appContextForRetry: Context? = null

    // ── Frequency capping ─────────────────────────────────────────────────────────
    // Full-screen formats (Interstitial, Rewarded Interstitial) are shown at genuine task-
    // completion moments, but a user can trigger those moments in quick succession (e.g. tapping
    // "Download another" repeatedly). Capping how often each format can actually show protects
    // against exactly the "excessive/disruptive full-screen ads" pattern AdMob policy prohibits —
    // which risks the whole account, i.e. all revenue, not just one impression.
    private object FrequencyGuard {
        private val lastShownMs = mutableMapOf<String, Long>()
        fun canShow(key: String, minIntervalMs: Long): Boolean =
            (Date().time - (lastShownMs[key] ?: 0L)) >= minIntervalMs
        fun recordShown(key: String) {
            lastShownMs[key] = Date().time
        }
    }

    private const val KEY_INTERSTITIAL = "interstitial"
    private const val KEY_REWARDED_INTERSTITIAL = "rewarded_interstitial"
    private const val MIN_INTERVAL_INTERSTITIAL_MS = 60_000L
    private const val MIN_INTERVAL_REWARDED_INTERSTITIAL_MS = 90_000L

    // ── Retry with backoff ───────────────────────────────────────────────────────
    // A transient load failure (offline for a second, momentary no-fill) otherwise stalls that
    // format until some unrelated later trigger happens to call preload again — losing fill that
    // a simple retry would have recovered. Backoff avoids hammering the network/SDK on a real
    // outage: 15s, 30s, 60s, ... capped at 5 minutes.
    private val retryHandler = Handler(Looper.getMainLooper())

    private fun retryWithBackoff(failureCount: Int, action: () -> Unit) {
        val delayMs = (15_000L * (1L shl failureCount.coerceIn(0, 5))).coerceAtMost(300_000L)
        retryHandler.postDelayed({ if (canShowAds()) action() }, delayMs)
    }

    // ── Consent (UMP) + SDK init ─────────────────────────────────────────────────

    /**
     * Requests up-to-date consent info, shows the UMP consent form if one is required for this
     * user's region, then initializes the Mobile Ads SDK **only if** [ConsentInformation.canRequestAds]
     * is true afterward — i.e. ads are never requested for a user who hasn't consented (or
     * doesn't need to, e.g. outside the EEA/UK, in which case this resolves immediately).
     * [onComplete] always fires so app navigation is never blocked on this.
     */
    fun initializeConsentAndAds(activity: Activity, onComplete: () -> Unit) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        val params = ConsentRequestParameters.Builder().build()
        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    maybeInitializeAds(activity)
                    onComplete()
                }
            },
            {
                // Couldn't refresh consent status (e.g. offline) — fall back to whatever was
                // previously known; if that's still "can request ads", proceed, otherwise skip.
                maybeInitializeAds(activity)
                onComplete()
            }
        )
    }

    private fun maybeInitializeAds(context: Context) {
        if (adsInitialized) return
        if (consentInformation?.canRequestAds() != true) return
        MobileAds.initialize(context) {}
        adsInitialized = true
    }

    /** Whether ads may be requested at all right now (consent obtained/not required, SDK initialized, and user hasn't disabled ads). */
    fun canShowAds(): Boolean = _adsEnabled.value && adsInitialized && consentInformation?.canRequestAds() == true

    /**
     * Whether Settings should show a "Manage ad consent" row — only relevant for users the UMP
     * form actually applies to (mainly EEA/UK/US-states regulation), so this stays hidden for
     * everyone else instead of showing a dead menu item.
     */
    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Re-opens the same consent/privacy-options form the user saw on first launch, for changing their mind later. */
    fun openPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { onDismissed() }
    }

    /** Called once, after the app is past onboarding — see the `appOpenAdsEnabled` doc above. */
    fun enableAppOpenAds() {
        appOpenAdsEnabled = true
    }

    /** Preloads Interstitial + Rewarded + Rewarded Interstitial + App Open. Safe to call even if [canShowAds] is false (no-ops). */
    fun preloadAll(context: Context) {
        appContextForRetry = context.applicationContext
        if (!canShowAds()) return
        preloadInterstitial(context)
        preloadRewarded(context)
        preloadRewardedInterstitial(context)
        appOpenAdManager.loadAd(context)
    }

    // ── Interstitial ──────────────────────────────────────────────────────────────

    private var interstitialAd: InterstitialAd? = null
    private var interstitialLoading = false
    private var interstitialFailCount = 0

    private fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || interstitialLoading || !canShowAds()) return
        interstitialLoading = true
        InterstitialAd.load(
            context,
            AdUnitIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoading = false
                    interstitialFailCount = 0
                    notifyEvent(AdEvent.AdLoaded(AdFormat.INTERSTITIAL))
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    interstitialLoading = false
                    notifyEvent(AdEvent.AdFailedToLoad(AdFormat.INTERSTITIAL, error.message, error.code))
                    retryWithBackoff(interstitialFailCount++) {
                        appContextForRetry?.let { preloadInterstitial(it) }
                    }
                }
            }
        )
    }

    /**
     * Shows the preloaded interstitial if one is ready AND the min-interval frequency cap has
     * elapsed (see [FrequencyGuard]); otherwise no-ops immediately (never blocks the caller
     * waiting for a load, and never shows two full-screen ads back-to-back).
     */
    fun showInterstitialIfAvailable(onDismissed: () -> Unit) {
        val activity = currentActivityRef?.get()
        val ad = interstitialAd
        if (activity == null || ad == null || !FrequencyGuard.canShow(KEY_INTERSTITIAL, MIN_INTERVAL_INTERSTITIAL_MS)) {
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyEvent(AdEvent.AdShown(AdFormat.INTERSTITIAL))
            }
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                FrequencyGuard.recordShown(KEY_INTERSTITIAL)
                notifyEvent(AdEvent.AdDismissed(AdFormat.INTERSTITIAL))
                preloadInterstitial(activity.applicationContext)
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                notifyEvent(AdEvent.AdFailedToLoad(AdFormat.INTERSTITIAL, error.message, error.code))
                onDismissed()
            }
        }
        ad.show(activity)
    }

    // ── Rewarded ──────────────────────────────────────────────────────────────────

    private var rewardedAd: RewardedAd? = null
    private var rewardedLoading = false
    private var rewardedFailCount = 0

    private fun preloadRewarded(context: Context) {
        if (rewardedAd != null || rewardedLoading || !canShowAds()) return
        rewardedLoading = true
        RewardedAd.load(
            context,
            AdUnitIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedLoading = false
                    rewardedFailCount = 0
                    notifyEvent(AdEvent.AdLoaded(AdFormat.REWARDED))
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    rewardedLoading = false
                    notifyEvent(AdEvent.AdFailedToLoad(AdFormat.REWARDED, error.message, error.code))
                    retryWithBackoff(rewardedFailCount++) {
                        appContextForRetry?.let { preloadRewarded(it) }
                    }
                }
            }
        )
    }

    /** [onRewardEarned] fires only if the user actually watched to completion; [onUnavailable] if no ad is ready yet. */
    fun showRewardedAd(onRewardEarned: () -> Unit, onUnavailable: () -> Unit) {
        val activity = currentActivityRef?.get()
        val ad = rewardedAd
        if (activity == null || ad == null) {
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyEvent(AdEvent.AdShown(AdFormat.REWARDED))
            }
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                notifyEvent(AdEvent.AdDismissed(AdFormat.REWARDED))
                preloadRewarded(activity.applicationContext)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                notifyEvent(AdEvent.AdFailedToLoad(AdFormat.REWARDED, error.message, error.code))
                onUnavailable()
            }
        }
        ad.show(activity) { rewardItem ->
            notifyEvent(AdEvent.RewardEarned(AdFormat.REWARDED, rewardItem.amount, rewardItem.type))
            onRewardEarned()
        }
    }

    // ── Rewarded Interstitial ────────────────────────────────────────────────────
    // A second full-screen-with-reward format: unlike plain Rewarded (only ever shown behind an
    // explicit "Watch ad" button), this one is *offered* at a natural break the same way an
    // Interstitial is, but still rewards the user for opting to watch — a genuinely additive
    // revenue surface rather than competing with the existing Rewarded placement. Frequency-
    // capped independently from plain Interstitial so the two don't stack on the same moment.

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var rewardedInterstitialLoading = false
    private var rewardedInterstitialFailCount = 0

    private fun preloadRewardedInterstitial(context: Context) {
        if (rewardedInterstitialAd != null || rewardedInterstitialLoading || !canShowAds()) return
        rewardedInterstitialLoading = true
        RewardedInterstitialAd.load(
            context,
            AdUnitIds.REWARDED_INTERSTITIAL,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    rewardedInterstitialLoading = false
                    rewardedInterstitialFailCount = 0
                    notifyEvent(AdEvent.AdLoaded(AdFormat.REWARDED_INTERSTITIAL))
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    rewardedInterstitialLoading = false
                    notifyEvent(AdEvent.AdFailedToLoad(AdFormat.REWARDED_INTERSTITIAL, error.message, error.code))
                    retryWithBackoff(rewardedInterstitialFailCount++) {
                        appContextForRetry?.let { preloadRewardedInterstitial(it) }
                    }
                }
            }
        )
    }

    /**
     * Shows the preloaded Rewarded Interstitial if one is ready and its own frequency cap has
     * elapsed; [onUnavailable] fires immediately with no ad shown otherwise. [onRewardEarned]
     * fires only if the user watches to completion — dismissing early still counts as "shown"
     * for the frequency cap, but earns nothing.
     */
    fun showRewardedInterstitialAd(onRewardEarned: () -> Unit, onUnavailable: () -> Unit) {
        val activity = currentActivityRef?.get()
        val ad = rewardedInterstitialAd
        if (activity == null || ad == null || !FrequencyGuard.canShow(KEY_REWARDED_INTERSTITIAL, MIN_INTERVAL_REWARDED_INTERSTITIAL_MS)) {
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyEvent(AdEvent.AdShown(AdFormat.REWARDED_INTERSTITIAL))
            }
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                FrequencyGuard.recordShown(KEY_REWARDED_INTERSTITIAL)
                notifyEvent(AdEvent.AdDismissed(AdFormat.REWARDED_INTERSTITIAL))
                preloadRewardedInterstitial(activity.applicationContext)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedInterstitialAd = null
                notifyEvent(AdEvent.AdFailedToLoad(AdFormat.REWARDED_INTERSTITIAL, error.message, error.code))
                onUnavailable()
            }
        }
        ad.show(activity) { rewardItem ->
            notifyEvent(AdEvent.RewardEarned(AdFormat.REWARDED_INTERSTITIAL, rewardItem.amount, rewardItem.type))
            onRewardEarned()
        }
    }

    // ── App Open ──────────────────────────────────────────────────────────────────

    private val appOpenAdManager = AppOpenAdManager()

    /**
     * Wires App Open ad display to real app-foregrounding (not just any Activity resume, which
     * would also fire right after closing an Interstitial/Rewarded ad within this same single-
     * Activity app — showing a second ad back-to-back). [ActivityLifecycleCallbacks] tracks which
     * Activity to show on; [ProcessLifecycleOwner] is the actual "app came back to foreground"
     * signal, per Google's own reference pattern. Called once from `MediaSaverApp.onCreate()`.
     */
    fun setupAppOpenAdLifecycleTracking(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    if (!appOpenAdManager.isShowingAd) currentActivityRef = WeakReference(activity)
                }
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            }
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (!appOpenAdsEnabled || !canShowAds()) return
                    currentActivityRef?.get()?.let { appOpenAdManager.showAdIfAvailable(it) }
                }
            }
        )
    }

    private class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false
            private set
        private var loadTimeMs = 0L
        private var failCount = 0

        fun clearAd() {
            appOpenAd = null
            isShowingAd = false
            isLoadingAd = false
        }

        fun loadAd(context: Context) {
            if (isLoadingAd || appOpenAd != null || !canShowAds()) return
            isLoadingAd = true
            AppOpenAd.load(
                context,
                AdUnitIds.APP_OPEN,
                AdRequest.Builder().build(),
                object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTimeMs = Date().time
                        failCount = 0
                        notifyEvent(AdEvent.AdLoaded(AdFormat.APP_OPEN))
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoadingAd = false
                        notifyEvent(AdEvent.AdFailedToLoad(AdFormat.APP_OPEN, error.message, error.code))
                        retryWithBackoff(failCount++) {
                            appContextForRetry?.let { loadAd(it) }
                        }
                    }
                }
            )
        }

        fun showAdIfAvailable(activity: Activity) {
            if (isShowingAd || !isAdFreshAndAvailable()) return
            val ad = appOpenAd ?: return
            isShowingAd = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    notifyEvent(AdEvent.AdShown(AdFormat.APP_OPEN))
                }
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    notifyEvent(AdEvent.AdDismissed(AdFormat.APP_OPEN))
                    loadAd(activity.applicationContext)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    notifyEvent(AdEvent.AdFailedToLoad(AdFormat.APP_OPEN, error.message, error.code))
                }
            }
            ad.show(activity)
        }

        // Google's own guidance: an App Open ad older than 4 hours is considered stale and
        // shouldn't be shown — reload instead.
        private fun isAdFreshAndAvailable(): Boolean =
            appOpenAd != null && (Date().time - loadTimeMs) < 4L * 3_600_000L
    }
}
