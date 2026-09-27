package com.mediasaver.app.data.platform

/** Platform-specific utilities — implemented via expect/actual per target. */

/** Returns the platform name string for diagnostics. */
expect fun platformName(): String

/** Returns a monotonic epoch millisecond timestamp. */
expect fun currentTimeMs(): Long

/**
 * Opens the containing folder of [filePath] in the native file explorer.
 * No-op on platforms where this is not supported.
 */
expect fun openFolder(filePath: String)

/** Returns a comparable "day bucket" (e.g. epoch day number) for grouping records by calendar date. */
expect fun dayBucket(epochMs: Long): Long

/** Formats [epochMs] as a human-readable date-section header, e.g. "Aug 29, 2025". */
expect fun formatDayHeader(epochMs: Long): String

/**
 * Opens the installed app identified by [packageName] (e.g. a social platform), or falls back
 * to [webFallbackUrl] in a browser if the app isn't installed.
 */
expect fun openApp(packageName: String, webFallbackUrl: String)

/** Opens the system share sheet for a previously-downloaded file at [filePath]. */
expect fun shareFile(filePath: String, mimeType: String)

enum class NetworkConnection {
    WIFI,
    CELLULAR,
    OFFLINE
}

/** True if the device currently has an active, non-metered (Wi-Fi) connection. */
expect fun isOnWifi(): Boolean

/** True if the device is currently connected via mobile cellular data. */
expect fun isOnCellular(): Boolean

/** True if an active internet connection is available. */
expect fun isOnline(): Boolean

/** Returns the current network connection type (Wi-Fi, Cellular, or Offline). */
expect fun getNetworkConnection(): NetworkConnection

/**
 * Reads the system clipboard's current text, once, and returns it only if it looks like a
 * downloadable http(s) URL — null otherwise (empty clipboard, non-URL text, or — on Android
 * 10+ — the app not being in focus, which the OS silently returns nothing for).
 *
 * Called only from a resume-triggered check (see HomeScreen), never on a timer/poll loop.
 */
expect fun readClipboardUrlIfPresent(): String?

/** Renames an already-downloaded file (identified by its stored `content://`/`file://` URI) to [newDisplayName]. */
expect fun renameDownloadedFile(filePath: String, newDisplayName: String): Boolean

/** Permanently deletes a downloaded file (identified by its stored `content://`/`file://` URI). */
expect fun deleteDownloadedFile(filePath: String): Boolean

/**
 * Shows a preloaded interstitial ad if one is ready and the user has consented to ads (see
 * [com.mediasaver.app.data.ads.AdsController]); otherwise [onDismissed] fires immediately with
 * no ad shown. Never blocks waiting for one to load.
 */
expect fun showInterstitialAdIfAvailable(onDismissed: () -> Unit)

/**
 * Shows a rewarded ad. [onRewardEarned] fires only if the user watches to completion;
 * [onAdUnavailable] fires immediately (no ad shown) if none is ready or ads aren't consented to.
 */
expect fun showRewardedAd(onRewardEarned: () -> Unit, onAdUnavailable: () -> Unit)

/**
 * Shows a Rewarded Interstitial ad — like [showRewardedAd] (reward only on full watch), but a
 * distinct ad unit/format from a distinct inventory pool, so it's an additive revenue surface
 * rather than competing with the plain Rewarded placement. Also frequency-capped independently
 * of Interstitial (see [com.mediasaver.app.data.ads.AdsController]).
 */
expect fun showRewardedInterstitialAd(onRewardEarned: () -> Unit, onAdUnavailable: () -> Unit)

/**
 * Sets whether all advertisements (banner, native, interstitial, rewarded, app open) are enabled
 * or disabled globally throughout the application.
 */
expect fun setAdsGloballyEnabled(enabled: Boolean)

/** Returns whether advertisements are currently enabled globally. */
expect fun areAdsGloballyEnabled(): Boolean

