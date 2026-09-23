package com.mediasaver.app.ui.state

import com.mediasaver.app.data.di.AppModule
import com.mediasaver.app.data.platform.currentTimeMs
import com.mediasaver.app.data.platform.isOnWifi
import com.mediasaver.app.data.platform.showRewardedAd
import com.mediasaver.app.data.platform.showRewardedInterstitialAd
import com.mediasaver.app.domain.model.AppSettings
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.model.PremiumPlan
import com.mediasaver.app.domain.model.ThemeMode
import com.mediasaver.app.domain.repository.DownloadRepository
import com.mediasaver.app.domain.repository.DownloadScheduler
import com.mediasaver.app.domain.repository.OnboardingStore
import com.mediasaver.app.domain.repository.PremiumStore
import com.mediasaver.app.domain.repository.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Single source of truth for the entire app's UI.
 *
 * Pure Kotlin — no Android/Desktop imports (except platform utils via expect/actual).
 *
 * Lifecycle:
 *  - Created once when the root composable enters composition.
 *  - [dispose] is called via DisposableEffect when the composition leaves.
 */
class AppViewModel(
    private val repository: DownloadRepository = AppModule.repository,
    private val onboardingStore: OnboardingStore = AppModule.onboardingStore,
    private val premiumStore: PremiumStore = AppModule.premiumStore,
    private val settingsStore: SettingsStore = AppModule.settingsStore,
    private val downloadScheduler: DownloadScheduler = AppModule.downloadScheduler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ── Public state ──────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Idle)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<DownloadRecord>>(emptyList())
    val history: StateFlow<List<DownloadRecord>> = _history.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _activePlan = MutableStateFlow<PremiumPlan?>(null)
    /** The locally-unlocked plan, or null if the user hasn't selected one. See [PremiumStore]. */
    val activePlan: StateFlow<PremiumPlan?> = _activePlan.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _temporaryUnlockExpiresAt = MutableStateFlow<Long?>(null)
    /** Epoch-ms expiry of a rewarded-ad-earned temporary Premium unlock, or null if none/expired. */
    val temporaryUnlockExpiresAt: StateFlow<Long?> = _temporaryUnlockExpiresAt.asStateFlow()

    // ── Private ───────────────────────────────────────────────────────────────
    private var downloadJob: Job? = null
    private var activeJobId: String? = null
    private var lastUrl: String = ""

    init {
        // Subscribe to the live history flow from the repository
        repository.history()
            .onEach { _history.value = it }
            .catch { /* history errors are non-fatal */ }
            .launchIn(scope)

        // Eagerly load persisted history from disk on startup
        scope.launch {
            repository.loadHistory()
        }

        scope.launch {
            _activePlan.value = premiumStore.activePlan()
            _temporaryUnlockExpiresAt.value = premiumStore.temporaryUnlockExpiresAt()
        }

        scope.launch {
            _settings.value = settingsStore.load()
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        scope.launch { settingsStore.save(updated) }
    }

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setWifiOnlyDownloads(enabled: Boolean) = updateSettings { it.copy(wifiOnlyDownloads = enabled) }
    fun setConfirmBeforeDelete(enabled: Boolean) = updateSettings { it.copy(confirmBeforeDelete = enabled) }
    fun setAskBeforeDownload(enabled: Boolean) = updateSettings { it.copy(askBeforeDownload = enabled) }
    fun setAutoDetectClipboard(enabled: Boolean) = updateSettings { it.copy(autoDetectClipboard = enabled) }

    // ── Navigation gating ─────────────────────────────────────────────────────

    /** Whether the one-time onboarding flow should be skipped. Read once by the nav graph's start destination. */
    suspend fun hasCompletedOnboarding(): Boolean = onboardingStore.hasCompletedOnboarding()

    fun markOnboardingCompleted() {
        scope.launch { onboardingStore.setOnboardingCompleted() }
    }

    // ── Premium plans ─────────────────────────────────────────────────────────

    /**
     * Persists [plan] as the locally-unlocked tier. This does not process any payment — see
     * [PremiumStore] for why — it only updates local state so the UI reflects the selection.
     */
    fun selectPlan(plan: PremiumPlan) {
        scope.launch {
            premiumStore.setActivePlan(plan)
            _activePlan.value = plan
        }
    }

    /**
     * Shows a rewarded ad; on completion, grants a real 24-hour Premium unlock (the actual
     * reward — not a fake "purchase," just an honest ad-for-access trade, since there's no
     * billing backend). [onResult] reports whether a reward was actually earned, so the UI can
     * show "ad unavailable, try again" vs. a success confirmation.
     *
     * Extends from the current unlock (if one is still active) rather than resetting it, so
     * watching a second ad genuinely adds value instead of being wasted — a small honest
     * incentive to watch more than one ad per visit.
     */
    fun watchRewardedAdForPremium(onResult: (earned: Boolean) -> Unit) {
        showRewardedAd(
            onRewardEarned = {
                scope.launch {
                    val base = maxOf(_temporaryUnlockExpiresAt.value ?: 0L, currentTimeMs())
                    val expiry = base + 24L * 3_600_000L
                    premiumStore.grantTemporaryUnlock(expiry)
                    _temporaryUnlockExpiresAt.value = expiry
                    onResult(true)
                }
            },
            onAdUnavailable = { onResult(false) }
        )
    }

    /**
     * Same trade as [watchRewardedAdForPremium] but via the Rewarded Interstitial format (a
     * separate inventory pool from plain Rewarded — see [com.mediasaver.app.data.ads.AdsController])
     * and a bigger 48-hour grant, since it's offered as the "watch a longer ad, get more" option.
     */
    fun watchRewardedInterstitialForPremium(onResult: (earned: Boolean) -> Unit) {
        showRewardedInterstitialAd(
            onRewardEarned = {
                scope.launch {
                    val base = maxOf(_temporaryUnlockExpiresAt.value ?: 0L, currentTimeMs())
                    val expiry = base + 48L * 3_600_000L
                    premiumStore.grantTemporaryUnlock(expiry)
                    _temporaryUnlockExpiresAt.value = expiry
                    onResult(true)
                }
            },
            onAdUnavailable = { onResult(false) }
        )
    }

    // ── Events (called from UI) ───────────────────────────────────────────────

    fun onUrlChanged(url: String) {
        _currentUrl.value = url
    }

    /**
     * Start media extraction for [url].
     * Transitions: Idle/Preview/Error → Loading → Preview | Error
     *
     * No-ops while a download is actively in progress — there's no queue yet (see
     * [onDownloadClick]'s single [downloadJob]), so starting a new extraction here would leave
     * the in-progress download running unmanaged in the background, silently clobbering whatever
     * the user is now looking at the moment it finishes or fails. [HomeScreen] disables the URL
     * input for the same reason; this is a second, ViewModel-level guard against that state ever
     * being reached some other way.
     */
    fun onUrlSubmit(url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        if (_uiState.value is AppUiState.Downloading) return
        lastUrl = trimmed
        _uiState.value = AppUiState.Loading

        scope.launch {
            try {
                val results = repository.extractMedia(trimmed)
                _uiState.value = if (results.isEmpty())
                    AppUiState.Error("No media found at this URL.", canRetry = false)
                else
                    AppUiState.Preview(results)
            } catch (e: Exception) {
                _uiState.value = AppUiState.Error(
                    message  = e.message ?: "Failed to extract media. Please check the URL.",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Enqueues [source] for [mediaInfo] as durable background work (see [DownloadScheduler]) and
     * observes its live status to drive [uiState] — the download itself now runs independently
     * of this ViewModel/the Activity's lifecycle, so it survives backgrounding, the screen being
     * destroyed, and (via WorkManager's own persistence) process death.
     * Transitions: Preview → Downloading → Success | Error
     */
    fun onDownloadClick(mediaInfo: MediaInfo, source: MediaSource) {
        if (_settings.value.wifiOnlyDownloads && !isOnWifi()) {
            _uiState.value = AppUiState.Error(
                message  = "Wi-Fi only is on in Settings, and you're not on Wi-Fi right now.",
                canRetry = false
            )
            return
        }

        downloadJob?.cancel()
        val jobId = "${mediaInfo.id}_${currentTimeMs()}"
        activeJobId = jobId
        downloadScheduler.enqueue(jobId, mediaInfo, source)

        downloadJob = scope.launch {
            downloadScheduler.observeStatus(jobId).collect { status ->
                when (status) {
                    is DownloadStatus.Queued      -> _uiState.value = AppUiState.Downloading(0, "…", mediaInfo)
                    is DownloadStatus.Downloading -> _uiState.value = AppUiState.Downloading(
                        status.progressPercent, status.speedFormatted, mediaInfo
                    )
                    is DownloadStatus.Merging     -> _uiState.value = AppUiState.Downloading(
                        100, "Merging…", mediaInfo
                    )
                    is DownloadStatus.Done -> {
                        _uiState.value = AppUiState.Success(status.filePath, mediaInfo)
                        downloadJob?.cancel() // terminal — stop observing this job's (long-lived) WorkInfo flow
                    }
                    is DownloadStatus.Failed -> {
                        _uiState.value = AppUiState.Error(status.reason, canRetry = true)
                        downloadJob?.cancel()
                    }
                    is DownloadStatus.Cancelled -> {
                        _uiState.value = AppUiState.Preview(listOf(mediaInfo))
                        downloadJob?.cancel()
                    }
                    else -> Unit
                }
            }
        }
    }

    /** Cancel an in-progress download (stops the real background work, not just this screen's view of it) and return to Preview state. */
    fun onCancel() {
        activeJobId?.let { downloadScheduler.cancel(it) }
        downloadJob?.cancel()
        downloadJob = null
        // Re-show the last preview if we have media info
        val prev = (_uiState.value as? AppUiState.Downloading)?.mediaInfo
        _uiState.value = if (prev != null) AppUiState.Preview(listOf(prev))
                         else AppUiState.Idle
    }

    /** Dismiss an error and return to Idle. */
    fun onDismissError() {
        _uiState.value = AppUiState.Idle
    }

    /** Retry the last URL extraction after an error. */
    fun onRetry() {
        if (lastUrl.isNotBlank()) onUrlSubmit(lastUrl)
    }

    /** Navigate back to Idle from Success or Preview. */
    fun onReset() {
        _uiState.value = AppUiState.Idle
        _currentUrl.value = ""
    }

    /** Clear all download history from memory and disk. */
    fun clearHistory() {
        scope.launch {
            repository.clearHistory()
        }
    }

    /**
     * Deletes [record] — for a completed download this removes the file on disk too, not just
     * the history entry (a failed record has no file, so only its history entry is removed).
     */
    fun deleteRecord(record: DownloadRecord) {
        scope.launch {
            repository.deleteRecordAndFile(record.id)
        }
    }

    /** Renames a completed download's file and updates its title. Returns success via [onResult]. */
    fun renameRecord(id: String, newTitle: String, onResult: (Boolean) -> Unit = {}) {
        scope.launch {
            onResult(repository.renameRecord(id, newTitle))
        }
    }

    /** Must be called when the composable leaves composition to avoid leaks. */
    fun dispose() {
        scope.cancel()
    }
}
