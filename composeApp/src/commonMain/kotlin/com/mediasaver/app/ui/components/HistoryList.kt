package com.mediasaver.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.ui.theme.*

/**
 * A single row in the Downloads list styled as a Bento surface card
 * with clear categorization badges (Video, Audio, Quality, Platform),
 * thumbnail overlay badges, and clean metadata without clutter.
 */
@Composable
fun DownloadRow(
    record: DownloadRecord,
    onOpenDetail: (DownloadRecord) -> Unit,
    onDeleteRequest: (DownloadRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val failed = record.outcome == DownloadOutcome.FAILED
    var menuExpanded by remember { mutableStateOf(false) }

    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) BentoCardDark else BentoCardWhite
    val cardBorder = if (isDark) BentoBorderDark else BentoBorderLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    val isAudio = record.selectedSource.mimeType.startsWith("audio/") ||
            record.selectedSource.ext in listOf("mp3", "m4a", "opus", "wav") ||
            record.selectedSource.label.contains("audio", ignoreCase = true)

    val qualityTag = record.selectedSource.qualityBadge
        ?: when {
            record.selectedSource.label.contains("1080") -> "1080p HD"
            record.selectedSource.label.contains("720")  -> "720p"
            record.selectedSource.label.contains("480")  -> "480p"
            record.selectedSource.label.contains("360")  -> "360p"
            isAudio -> "AUDIO"
            else -> record.selectedSource.label
        }

    val isHd = qualityTag.contains("HD") || qualityTag.contains("4K") || qualityTag.contains("QHD") || qualityTag.contains("60")

    BentoSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(record) },
        backgroundColor = cardBg,
        borderColor = cardBorder,
        cornerRadius = 20.dp,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail container with Category Overlay Chip
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF1E2130) else BentoSkyContainer),
                contentAlignment = Alignment.Center
            ) {
                if (record.mediaInfo.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = record.mediaInfo.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = if (isAudio) BentoSkyText else BentoPurplePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Thumbnail bottom overlay chip: Category / Quality indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 4.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = if (isAudio) "AUDIO" else (record.selectedSource.resolution ?: if (isHd) "HD" else "VIDEO"),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = record.mediaInfo.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textPrimary
                )

                Spacer(Modifier.height(4.dp))

                // Row of clear categorized badges!
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Badge (Video vs Audio)
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isAudio) {
                            if (isDark) Color(0xFF1B2640) else BentoSkyContainer
                        } else {
                            if (isDark) Color(0xFF281F3E) else BentoLavenderContainer
                        }
                    ) {
                        Text(
                            text = if (isAudio) "🎵 Audio" else "🎬 Video",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAudio) {
                                if (isDark) Color(0xFF93C5FD) else BentoSkyText
                            } else {
                                if (isDark) Color(0xFFD8B4FE) else BentoPurplePrimary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Quality Badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isHd) {
                            if (isDark) Color(0xFF332014) else BentoPeachContainer
                        } else {
                            if (isDark) Color(0xFF202332) else Color(0xFFEFF1F8)
                        }
                    ) {
                        Text(
                            text = qualityTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHd) {
                                if (isDark) Color(0xFFFDBA74) else BentoPeachText
                            } else {
                                textSecondary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Platform Tag
                    Text(
                        text = "· ${record.mediaInfo.platform.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                // File size & status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (failed) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Failed · Tap for details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ErrorRed
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF86EFAC) else BentoSageText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${record.selectedSource.fileSizeFormatted ?: "Complete"} · Saved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }
            }

            // Quick actions: Open in folder & 3-dots Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!failed) {
                    Surface(
                        onClick = { openFolder(record.filePath) },
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF272144) else BentoPurpleContainer.copy(alpha = 0.7f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = "Open file",
                                tint = BentoPurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Box {
                    Surface(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF222434) else Color(0xFFF1F3F8)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (!failed) {
                            DropdownMenuItem(
                                text = { Text("Open with Player") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                onClick = { menuExpanded = false; openFolder(record.filePath) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share File") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = { menuExpanded = false; shareFile(record.filePath, record.selectedSource.mimeType) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = { menuExpanded = false; onDeleteRequest(record) }
                        )
                    }
                }
            }
        }
    }
}
