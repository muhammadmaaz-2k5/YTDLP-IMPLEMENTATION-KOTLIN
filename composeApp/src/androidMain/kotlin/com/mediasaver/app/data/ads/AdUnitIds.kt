package com.mediasaver.app.data.ads

/**
 * GOOGLE'S OWN PUBLISHED TEST AD UNIT IDS — https://developers.google.com/admob/android/test-ads
 *
 * These are safe to ship during development (they always fill with a placeholder "Test Ad" and
 * never earn revenue), but must be replaced with your own ad units from the AdMob console
 * (one per format, matching the App ID in AndroidManifest.xml) before a real release. Requesting
 * real ads during development risks invalid-traffic flags on your AdMob account — don't swap
 * these out until you're actually ready to ship.
 */
object AdUnitIds {
    // Anchored adaptive banner — wider/taller than the old fixed 320x50 size, which Google's own
    // adaptive-banner guidance recommends specifically because it fills more of the available
    // width and typically clears a higher eCPM than a fixed-size banner in the same slot.
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    // Interstitial-style timing (shows unprompted at a natural break) but still offers a reward
    // for watching — a second, higher-value full-screen format alongside plain Rewarded.
    const val REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
}
