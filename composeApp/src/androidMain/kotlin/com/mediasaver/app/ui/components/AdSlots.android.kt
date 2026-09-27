package com.mediasaver.app.ui.components

import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mediasaver.app.data.ads.AdEvent
import com.mediasaver.app.data.ads.AdFormat
import com.mediasaver.app.data.ads.AdUnitIds
import com.mediasaver.app.data.ads.AdsController

@Composable
actual fun BannerAdSlot(modifier: Modifier) {
    val adsEnabled by AdsController.adsEnabled.collectAsState()
    if (!adsEnabled || !AdsController.canShowAds()) return
    val context = LocalContext.current
    var loadFailed by remember { mutableStateOf(false) }
    if (loadFailed) return

    // Anchored adaptive size (full device width, height chosen by AdMob for that width) instead
    // of a fixed 320x50 — Google's own guidance is that adaptive banners fill more of the slot
    // and typically clear a meaningfully higher eCPM than a fixed size in the same placement.
    val adView = remember {
        val metrics = context.resources.displayMetrics
        val adWidthDp = (metrics.widthPixels / metrics.density).toInt()
        AdView(context).apply {
            adUnitId = AdUnitIds.BANNER
            setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, adWidthDp))
        }
    }

    DisposableEffect(adView) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdsController.notifyEvent(AdEvent.AdLoaded(AdFormat.BANNER))
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                loadFailed = true
                AdsController.notifyEvent(AdEvent.AdFailedToLoad(AdFormat.BANNER, error.message, error.code))
            }
            override fun onAdClicked() {
                AdsController.notifyEvent(AdEvent.AdClicked(AdFormat.BANNER))
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }

    if (!loadFailed) {
        AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
    }
}

@Composable
actual fun NativeAdCard(modifier: Modifier) {
    val adsEnabled by AdsController.adsEnabled.collectAsState()
    if (!adsEnabled || !AdsController.canShowAds()) return
    val context = LocalContext.current
    val density = LocalDensity.current

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    // Colors read here (in the composable, where MaterialTheme is available) and handed to the
    // plain-Android-View tree built below — the native ad view itself isn't Compose, so it can't
    // read MaterialTheme directly.
    val cardBg = MaterialTheme.colorScheme.surface.toArgb()
    val titleColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val ctaBg = MaterialTheme.colorScheme.primary.toArgb()
    val ctaText = MaterialTheme.colorScheme.onPrimary.toArgb()
    val badgeBg = MaterialTheme.colorScheme.surfaceVariant.toArgb()
    val badgeText = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    DisposableEffect(Unit) {
        val loader = AdLoader.Builder(context, AdUnitIds.NATIVE)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    AdsController.notifyEvent(AdEvent.AdLoaded(AdFormat.NATIVE))
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadFailed = true
                    AdsController.notifyEvent(AdEvent.AdFailedToLoad(AdFormat.NATIVE, error.message, error.code))
                }
                override fun onAdClicked() {
                    AdsController.notifyEvent(AdEvent.AdClicked(AdFormat.NATIVE))
                }
            })
            // Landscape media (image/video) — native ads with a media asset typically clear a
            // higher eCPM than text-only native, and most fill responses include one.
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .build()
        loader.loadAd(AdRequest.Builder().build())
        onDispose { nativeAd?.destroy() }
    }

    val ad = nativeAd
    if (loadFailed || ad == null) return

    fun dp(value: Int): Int = with(density) { value.dp.roundToPx() }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            val adView = NativeAdView(ctx)

            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(cardBg)
                }
            }

            val badge = TextView(ctx).apply {
                text = "Ad"
                textSize = 10f
                setTextColor(badgeText)
                setPadding(dp(8), dp(2), dp(8), dp(2))
                background = GradientDrawable().apply {
                    cornerRadius = dp(6).toFloat()
                    setColor(badgeBg)
                }
            }
            root.addView(badge, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10)
            })

            // Rounded container clips MediaView's image/video to match the card's corner radius
            // (MediaView itself has no shape/background support). Hidden in `update` when a
            // native ad response has no media content (text-only fill).
            val mediaContainer = FrameLayout(ctx).apply {
                clipToOutline = true
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, dp(14).toFloat())
                    }
                }
            }
            val mediaView = MediaView(ctx).apply {
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }
            mediaContainer.addView(mediaView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            root.addView(mediaContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(160)).apply {
                bottomMargin = dp(10)
            })

            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            }
            row.addView(icon)

            val textCol = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(12)
                }
            }
            val headline = TextView(ctx).apply {
                textSize = 15f
                setTextColor(titleColor)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                maxLines = 1
            }
            val body = TextView(ctx).apply {
                textSize = 13f
                setTextColor(bodyColor)
                maxLines = 2
            }
            textCol.addView(headline)
            textCol.addView(body)
            row.addView(textCol)
            root.addView(row)

            val cta = Button(ctx).apply {
                textSize = 14f
                setTextColor(ctaText)
                isAllCaps = false
                background = GradientDrawable().apply {
                    cornerRadius = dp(100).toFloat()
                    setColor(ctaBg)
                }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)).apply {
                    topMargin = dp(14)
                }
            }
            root.addView(cta)

            adView.addView(root)
            adView.headlineView = headline
            adView.bodyView = body
            adView.callToActionView = cta
            adView.iconView = icon
            adView.mediaView = mediaView

            adView
        },
        update = { view ->
            val nav = view as NativeAdView
            (nav.headlineView as TextView).text = ad.headline
            val bodyView = nav.bodyView as TextView
            if (ad.body.isNullOrBlank()) {
                bodyView.visibility = View.GONE
            } else {
                bodyView.visibility = View.VISIBLE
                bodyView.text = ad.body
            }
            (nav.callToActionView as Button).text = ad.callToAction ?: "Learn more"
            val iconView = nav.iconView as ImageView
            val icon = ad.icon
            if (icon != null) {
                iconView.setImageDrawable(icon.drawable)
                iconView.visibility = View.VISIBLE
            } else {
                iconView.visibility = View.GONE
            }
            val media = nav.mediaView as MediaView
            val mediaContent = ad.mediaContent
            (media.parent as View).visibility = if (mediaContent != null) View.VISIBLE else View.GONE
            if (mediaContent != null) media.mediaContent = mediaContent
            nav.setNativeAd(ad)
        }
    )
}
