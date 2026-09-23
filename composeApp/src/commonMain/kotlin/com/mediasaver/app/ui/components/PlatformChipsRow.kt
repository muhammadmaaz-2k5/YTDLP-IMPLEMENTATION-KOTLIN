package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mediasaver.app.data.platform.openApp
import com.mediasaver.app.ui.theme.FacebookChip
import com.mediasaver.app.ui.theme.InstagramChipEnd
import com.mediasaver.app.ui.theme.InstagramChipMid
import com.mediasaver.app.ui.theme.InstagramChipStart
import com.mediasaver.app.ui.theme.PinterestChip
import com.mediasaver.app.ui.theme.XChip
import com.mediasaver.app.ui.theme.YouTubeChip
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Facebook
import compose.icons.simpleicons.Instagram
import compose.icons.simpleicons.Pinterest
import compose.icons.simpleicons.Twitter
import compose.icons.simpleicons.Youtube

private data class PlatformChip(
    val label: String,
    val background: Brush,
    val icon: ImageVector,
    val packageName: String,
    val webFallbackUrl: String
)

private val PLATFORM_CHIPS = listOf(
    PlatformChip(
        label          = "YouTube",
        background     = Brush.linearGradient(listOf(YouTubeChip, YouTubeChip)),
        icon           = SimpleIcons.Youtube,
        packageName    = "com.google.android.youtube",
        webFallbackUrl = "https://www.youtube.com"
    ),
    PlatformChip(
        label          = "Instagram",
        background     = Brush.linearGradient(listOf(InstagramChipStart, InstagramChipMid, InstagramChipEnd)),
        icon           = SimpleIcons.Instagram,
        packageName    = "com.instagram.android",
        webFallbackUrl = "https://www.instagram.com"
    ),
    PlatformChip(
        label          = "Facebook",
        background     = Brush.linearGradient(listOf(FacebookChip, FacebookChip)),
        icon           = SimpleIcons.Facebook,
        packageName    = "com.facebook.katana",
        webFallbackUrl = "https://www.facebook.com"
    ),
    PlatformChip(
        // The bundled brand-icon pack predates the Twitter → X rebrand, so this renders the
        // legacy bird mark; the visible label still says "X" to match the app's current name.
        label          = "X",
        background     = Brush.linearGradient(listOf(XChip, XChip)),
        icon           = SimpleIcons.Twitter,
        packageName    = "com.twitter.android",
        webFallbackUrl = "https://x.com"
    ),
    PlatformChip(
        label          = "Pinterest",
        background     = Brush.linearGradient(listOf(PinterestChip, PinterestChip)),
        icon           = SimpleIcons.Pinterest,
        packageName    = "com.pinterest",
        webFallbackUrl = "https://www.pinterest.com"
    )
)

/** Row of rounded-square platform icons — tapping one opens that app (or its website as fallback). */
@Composable
fun PlatformChipsRow(modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PLATFORM_CHIPS.forEach { chip ->
            PlatformChipItem(chip)
        }
    }
}

@Composable
private fun PlatformChipItem(chip: PlatformChip) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(chip.background)
                .clickable { openApp(chip.packageName, chip.webFallbackUrl) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = chip.icon,
                contentDescription = chip.label,
                tint               = Color.White,
                modifier           = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = chip.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
