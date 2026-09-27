package com.mediasaver.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Premium promo card using the BentoOrganicCard with fluid wave curves and crisp white pill button,
 * dynamically reflecting the user's active membership perks and PRO status.
 */
@Composable
fun PremiumBannerCard(
    isPremiumActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPremiumActive) {
        BentoOrganicCard(
            title = "PRO Active",
            subtitle = "Watermark-free · Highest 4K resolution · Turbo download speed",
            buttonText = "Perks & Plans",
            onButtonClick = onClick,
            modifier = modifier,
            height = 180.dp
        )
    } else {
        BentoOrganicCard(
            title = "Unlock Premium",
            subtitle = "Watermark-free · High resolution · Fast speed",
            buttonText = "Upgrade",
            onButtonClick = onClick,
            modifier = modifier,
            height = 180.dp
        )
    }
}
