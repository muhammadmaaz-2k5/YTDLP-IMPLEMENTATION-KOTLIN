package com.mediasaver.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mediasaver.app.ui.theme.HeroGradient

private data class Feature(val icon: ImageVector, val label: String)

private val FEATURES = listOf(
    Feature(Icons.Default.MoneyOff, "Free"),
    Feature(Icons.Default.Bolt, "Fast"),
    Feature(Icons.Default.Lock, "On-device")
)

/** Idle hero state — shown before the user pastes any URL. */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "emptyFade"
    )

    Column(
        modifier            = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier.size(88.dp).clip(CircleShape).background(HeroGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Link,
                contentDescription = "No URL pasted yet",
                modifier           = Modifier.size(40.dp),
                tint               = Color.White
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text       = "One-Tap Fast\nDownload",
            style      = MaterialTheme.typography.displaySmall,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = "Paste a link from Facebook, Instagram, YouTube, TikTok and more — download in seconds",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(FEATURES.size) { i ->
                FeatureChip(FEATURES[i])
            }
        }
    }
}

@Composable
private fun FeatureChip(feature: Feature) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector        = feature.icon,
                contentDescription = null,
                modifier           = Modifier.size(16.dp),
                tint               = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text       = feature.label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
