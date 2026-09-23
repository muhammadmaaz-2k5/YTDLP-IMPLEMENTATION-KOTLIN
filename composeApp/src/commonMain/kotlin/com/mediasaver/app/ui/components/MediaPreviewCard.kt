package com.mediasaver.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.ui.theme.HeroGradient

/**
 * Card showing extracted media info with a quality selector and Download CTA.
 *
 * @param mediaInfo  The extracted media metadata.
 * @param onDownload Called with the user-selected [MediaSource] when Download is tapped.
 */
@Composable
fun MediaPreviewCard(
    mediaInfo: MediaInfo,
    onDownload: (MediaSource) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSource by remember(mediaInfo) {
        mutableStateOf(mediaInfo.bestSource ?: mediaInfo.sources.firstOrNull())
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // ── Thumbnail area ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(HeroGradient),
                contentAlignment = Alignment.Center
            ) {
                if (mediaInfo.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model              = mediaInfo.thumbnailUrl,
                        contentDescription = "Media thumbnail",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }

                // Play button — always shown for video-like content, over the thumbnail.
                if (mediaInfo.type != MediaType.IMAGE) {
                    Box(
                        modifier         = Modifier.size(56.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier           = Modifier.size(30.dp),
                            tint               = Color.White
                        )
                    }
                } else if (mediaInfo.thumbnailUrl.isBlank()) {
                    Icon(
                        imageVector        = Icons.Default.Image,
                        contentDescription = "Image",
                        modifier           = Modifier.size(64.dp),
                        tint               = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Platform + type badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text     = "${mediaInfo.platform.displayName} · ${mediaInfo.type.label}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                // Duration badge
                mediaInfo.durationSeconds?.let { dur ->
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text     = "%d:%02d".format(dur / 60, dur % 60),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color.White
                            )
                        }
                    }
                }
            }

            // ── Info + download section ─────────────────────────────────────
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text     = mediaInfo.title,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurface
                )
                mediaInfo.uploader?.let { uploader ->
                    Text(
                        text     = uploader,
                        style    = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Format/size badges for the currently-selected quality
                selectedSource?.let { src ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoBadge(src.ext.uppercase())
                        if (src.requiresMerge) InfoBadge("HD Merge")
                        src.fileSizeFormatted?.let { InfoBadge(it) }
                    }
                }

                // Quality selector (only shown when multiple sources available)
                if (mediaInfo.sources.size > 1 && selectedSource != null) {
                    Box {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            shape   = MaterialTheme.shapes.small
                        ) {
                            Text(
                                buildString {
                                    append(selectedSource!!.label)
                                    selectedSource!!.fileSizeFormatted?.let { append(" · $it") }
                                }
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select quality", modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded         = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            mediaInfo.sources.forEach { src ->
                                DropdownMenuItem(
                                    text = {
                                        Text(buildString {
                                            append(src.label)
                                            src.fileSizeFormatted?.let { append(" ($it)") }
                                        })
                                    },
                                    onClick = {
                                        selectedSource   = src
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick  = { selectedSource?.let(onDownload) },
                    enabled  = selectedSource != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Download ${selectedSource?.label ?: ""}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
