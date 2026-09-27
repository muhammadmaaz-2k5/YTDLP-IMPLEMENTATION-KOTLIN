package com.mediasaver.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.ui.theme.*

private enum class PreviewMode {
    VIDEO,
    AUDIO,
    THUMBNAIL
}

/**
 * Advanced, responsive Bento Media Preview Card featuring:
 * - 3-Way Mode Switcher: Video (MP4) | Audio Only | HD Thumbnail
 * - Direct 1-tap "Save Thumbnail" action
 * - Viral Hooks & Keywords Bento Tool: 1-tap copy for Hook/Title, Tags/Hashtags, and Caption
 * - Responsive FlowRow quality chips with zero horizontal or vertical overflow
 * - One-tap primary download action with exact target indicator
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaPreviewCard(
    mediaInfo: MediaInfo,
    onDownload: (MediaSource) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val allSources = mediaInfo.sources

    // Dedicated thumbnail source
    val thumbnailSource = remember(mediaInfo) {
        allSources.firstOrNull { it.formatId == "thumbnail" }
            ?: MediaSource(
                label = "HD Thumbnail Image",
                url = mediaInfo.thumbnailUrl,
                mimeType = "image/jpeg",
                formatId = "thumbnail",
                ext = "jpg",
                fileSizeBytes = null
            )
    }

    // Partition into video and audio sources (excluding thumbnail from video list)
    val audioSources = remember(allSources) {
        allSources.filter {
            it.formatId != "thumbnail" &&
                (it.mimeType.startsWith("audio/") || it.ext in listOf("m4a", "mp3", "opus", "wav") || it.label.contains("audio", ignoreCase = true))
        }
    }
    val videoSources = remember(allSources) {
        allSources.filter { it !in audioSources && it.formatId != "thumbnail" }
    }

    var selectedMode by remember(mediaInfo) {
        mutableStateOf(
            when {
                videoSources.isNotEmpty() -> PreviewMode.VIDEO
                audioSources.isNotEmpty() -> PreviewMode.AUDIO
                else -> PreviewMode.THUMBNAIL
            }
        )
    }

    val currentList = when (selectedMode) {
        PreviewMode.VIDEO -> if (videoSources.isNotEmpty()) videoSources else allSources
        PreviewMode.AUDIO -> if (audioSources.isNotEmpty()) audioSources else allSources
        PreviewMode.THUMBNAIL -> listOf(thumbnailSource)
    }

    var selectedSource by remember(currentList, selectedMode) {
        mutableStateOf(currentList.firstOrNull())
    }

    // Copy status feedback states
    var hookCopied by remember { mutableStateOf(false) }
    var tagsCopied by remember { mutableStateOf(false) }
    var captionCopied by remember { mutableStateOf(false) }
    var showToolsExpanded by remember { mutableStateOf(false) }

    // Curated tags: extracted tags, or fallback hashtags from title words
    val effectiveTags = remember(mediaInfo) {
        if (mediaInfo.tags.isNotEmpty()) {
            mediaInfo.tags
        } else {
            // Generate clean hashtag keywords from title
            mediaInfo.title.split(" ")
                .map { it.replace(Regex("[^A-Za-z0-9]"), "") }
                .filter { it.length in 3..20 }
                .distinct()
                .take(10)
        }
    }

    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        cornerRadius = 28.dp,
        backgroundColor = BentoSkyContainer,
        borderColor = Color.Transparent,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Top Header Row: Platform Badge + Dismiss Button ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Platform tag in white pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = mediaInfo.platform.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSkyText
                        )
                        Text(
                            text = "·",
                            color = BentoSkySubtext,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mediaInfo.type.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoSkySubtext
                        )
                    }
                }

                if (onCancel != null) {
                    Surface(
                        onClick = onCancel,
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.85f),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = BentoSkyText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ── Thumbnail Container with Quick-Save Button ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (mediaInfo.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = mediaInfo.thumbnailUrl,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Play icon overlay for video
                if (mediaInfo.type != MediaType.IMAGE && selectedMode != PreviewMode.THUMBNAIL) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else if (mediaInfo.thumbnailUrl.isBlank()) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image",
                        tint = BentoSkySubtext,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Quick 1-tap "Save Thumbnail" pill overlay (bottom start)
                if (mediaInfo.thumbnailUrl.isNotBlank()) {
                    Surface(
                        onClick = { onDownload(thumbnailSource) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(100.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Save Thumbnail",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Duration badge (bottom end)
                mediaInfo.durationSeconds?.let { dur ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(100.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "%d:%02d".format(dur / 60, dur % 60),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Title & Creator Header ────────────────────────────────────────
            Column {
                Text(
                    text = mediaInfo.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = BentoSkyText
                )
                mediaInfo.uploader?.let { uploader ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = uploader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoSkySubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Viral Content Tools: Hooks, Tags & Keywords (Expandable Bento) ──
            Surface(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.85f),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header row: Title + expand button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showToolsExpanded = !showToolsExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = BentoPurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Viral Hooks & Tags Tool",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimaryLight
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showToolsExpanded) "Less" else "Copy options",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPurplePrimary
                            )
                            Icon(
                                imageVector = if (showToolsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = BentoPurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Quick 1-tap Copy Action Pills (Always visible & responsive)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy Hook
                        ToolPillButton(
                            label = if (hookCopied) "Hook Copied ✓" else "Copy Hook",
                            isCopied = hookCopied,
                            onClick = {
                                clipboard.setText(AnnotatedString(mediaInfo.title))
                                hookCopied = true
                            }
                        )

                        // Copy Tags
                        if (effectiveTags.isNotEmpty()) {
                            ToolPillButton(
                                label = if (tagsCopied) "Tags Copied ✓" else "Copy Tags",
                                isCopied = tagsCopied,
                                onClick = {
                                    val formattedTags = effectiveTags.joinToString(" ") {
                                        if (it.startsWith("#")) it else "#$it"
                                    }
                                    clipboard.setText(AnnotatedString(formattedTags))
                                    tagsCopied = true
                                }
                            )
                        }

                        // Copy Caption (if available)
                        if (!mediaInfo.description.isNullOrBlank()) {
                            ToolPillButton(
                                label = if (captionCopied) "Caption Copied ✓" else "Copy Caption",
                                isCopied = captionCopied,
                                onClick = {
                                    clipboard.setText(AnnotatedString(mediaInfo.description))
                                    captionCopied = true
                                }
                            )
                        }
                    }

                    // Expanded Section: Tag Chips & Caption Preview
                    AnimatedVisibility(visible = showToolsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Tags Chips Preview
                            if (effectiveTags.isNotEmpty()) {
                                Text(
                                    text = "Keywords & Hashtags:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextSecondaryLight
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    effectiveTags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = BentoLavenderContainer.copy(alpha = 0.7f),
                                            modifier = Modifier.clickable {
                                                val t = if (tag.startsWith("#")) tag else "#$tag"
                                                clipboard.setText(AnnotatedString(t))
                                                tagsCopied = true
                                            }
                                        ) {
                                            Text(
                                                text = if (tag.startsWith("#")) tag else "#$tag",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BentoPurplePrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Caption Preview Snippet
                            if (!mediaInfo.description.isNullOrBlank()) {
                                Text(
                                    text = "Post Caption:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextSecondaryLight
                                )
                                Text(
                                    text = mediaInfo.description.take(240) + if (mediaInfo.description.length > 240) "…" else "",
                                    fontSize = 11.sp,
                                    color = BentoTextSecondaryLight,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── 3-Way Mode Switcher: Video | Audio | Thumbnail ────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Video Tab
                ModeTab(
                    title = "Video",
                    icon = Icons.Default.Videocam,
                    isSelected = selectedMode == PreviewMode.VIDEO,
                    onClick = {
                        selectedMode = PreviewMode.VIDEO
                        selectedSource = videoSources.firstOrNull() ?: allSources.firstOrNull()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Audio Tab
                ModeTab(
                    title = "Audio",
                    icon = Icons.Default.Audiotrack,
                    isSelected = selectedMode == PreviewMode.AUDIO,
                    onClick = {
                        selectedMode = PreviewMode.AUDIO
                        selectedSource = audioSources.firstOrNull() ?: allSources.lastOrNull()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Thumbnail Tab
                ModeTab(
                    title = "Thumbnail",
                    icon = Icons.Default.Image,
                    isSelected = selectedMode == PreviewMode.THUMBNAIL,
                    onClick = {
                        selectedMode = PreviewMode.THUMBNAIL
                        selectedSource = thumbnailSource
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Quality Option Chips (Responsive FlowRow) ─────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (selectedMode) {
                        PreviewMode.VIDEO -> "Select Video Quality"
                        PreviewMode.AUDIO -> "Select Audio Bitrate"
                        PreviewMode.THUMBNAIL -> "High-Definition Cover Image"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSkySubtext
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentList.forEach { source ->
                        val isSelected = source == selectedSource
                        Surface(
                            onClick = { selectedSource = source },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) BentoPurplePrimary else Color.White,
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier.animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = source.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoSkyText
                                )
                                if (source.isHighQuality) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else BentoPeachContainer
                                    ) {
                                        Text(
                                            text = "HQ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) Color.White else BentoPeachText,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                source.fileSizeFormatted?.let { size ->
                                    Text(
                                        text = "· $size",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else BentoSkySubtext
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Big Primary Download CTA Button ──────────────────────────────
            val active = selectedSource ?: currentList.firstOrNull() ?: thumbnailSource
            Button(
                onClick = { onDownload(active) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPurplePrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = when (selectedMode) {
                        PreviewMode.THUMBNAIL -> Icons.Default.Image
                        else -> Icons.Default.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = buildString {
                        if (selectedMode == PreviewMode.THUMBNAIL) {
                            append("Download HD Thumbnail")
                        } else {
                            append("Download ")
                            append(active.label)
                            active.fileSizeFormatted?.let { append(" ($it)") }
                        }
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ToolPillButton(
    label: String,
    isCopied: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (isCopied) BentoSageContainer else BentoLavenderContainer,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                tint = if (isCopied) BentoSageText else BentoPurplePrimary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCopied) BentoSageText else BentoPurplePrimary
            )
        }
    }
}

@Composable
private fun ModeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BentoPurplePrimary else BentoSkySubtext,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BentoSkyText else BentoSkySubtext,
                maxLines = 1
            )
        }
    }
}
