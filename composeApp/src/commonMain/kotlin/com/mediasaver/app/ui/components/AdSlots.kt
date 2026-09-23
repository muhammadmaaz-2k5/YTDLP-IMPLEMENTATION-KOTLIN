package com.mediasaver.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A standard AdMob banner ad. Renders nothing (zero height) if ads aren't consented to/available
 * yet — never reserves blank space for an ad that isn't there.
 */
@Composable
expect fun BannerAdSlot(modifier: Modifier = Modifier)

/**
 * A native ad styled as one more card in the feed, matching the app's card language (rounded
 * corners, brand colors) rather than a generic default layout. Renders nothing if unavailable —
 * same contract as [BannerAdSlot].
 */
@Composable
expect fun NativeAdCard(modifier: Modifier = Modifier)
