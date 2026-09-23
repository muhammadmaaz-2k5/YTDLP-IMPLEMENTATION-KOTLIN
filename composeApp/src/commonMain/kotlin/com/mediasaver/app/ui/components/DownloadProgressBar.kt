package com.mediasaver.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.ui.theme.HeroGradient

/**
 * Download progress card shown during [AppUiState.Downloading].
 *
 * @param mediaInfo       The item being downloaded.
 * @param progressPercent 0–100.
 * @param speedFormatted  Human-readable speed, e.g. "1.4 MB/s".
 * @param onCancel        Called when the user taps Cancel.
 */
@Composable
fun DownloadProgressBar(
    mediaInfo: MediaInfo,
    progressPercent: Int,
    speedFormatted: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progressPercent / 100f,
        animationSpec = tween(300),
        label         = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = mediaInfo.title,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "$progressPercent%",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text     = "${mediaInfo.platform.displayName} · downloading…",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(Modifier.height(16.dp))
            // Custom gradient-filled progress bar (stock LinearProgressIndicator can't take a Brush).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(50))
                        .background(HeroGradient)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = speedFormatted,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = onCancel,
                    shape   = MaterialTheme.shapes.extraLarge,
                    colors  = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel")
                }
            }
        }
    }
}
