package com.mediasaver.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mediasaver.app.data.platform.areAdsGloballyEnabled

/**
 * The single, persistent banner-ad placement in the app — a strip docked to the true bottom of
 * the screen that never scrolls away, as opposed to being embedded inline in scrollable content
 * (the previous approach on Home/Downloads/Settings, which meant the ad disappeared the moment
 * the user scrolled past it). Every screen that shows a banner ad uses this one composable, so
 * there's exactly one banner placement pattern in the whole app, not one per screen.
 *
 * Renders as an empty (zero-height) [Surface] when [BannerAdSlot] itself has nothing to show
 * (no consent, not yet loaded, or failed to load) — same "renders nothing if unavailable"
 * contract [BannerAdSlot] already documents, just wrapped with a docked-bottom-bar background.
 */
@Composable
fun StickyBannerBar(modifier: Modifier = Modifier) {
    if (!areAdsGloballyEnabled()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier         = Modifier.fillMaxWidth().navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            BannerAdSlot(modifier = Modifier.fillMaxWidth())
        }
    }
}
