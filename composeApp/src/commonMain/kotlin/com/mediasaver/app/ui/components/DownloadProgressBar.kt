package com.mediasaver.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.ui.theme.*

/**
 * State-of-the-Art Bento Download Progress Card featuring:
 * - Fluid shimmering gradient progress capsule
 * - Adaptive Light / Dark Bento card container
 * - Large legible media thumbnail & non-clipped multiline title
 * - Connected 4-stage pipeline stepper (Connect ➔ Download ➔ Process ➔ Save)
 * - Live speed pill, transferred bytes, and background safety badge
 * - Native cancel confirmation dialog
 */
@Composable
fun DownloadProgressBar(
    mediaInfo: MediaInfo,
    progressPercent: Int,
    speedFormatted: String,
    bytesDownloaded: Long = 0L,
    totalBytes: Long = 0L,
    isMerging: Boolean = false,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val merging = isMerging || progressPercent >= 100 || speedFormatted.contains("merge", ignoreCase = true)
    var showCancelConfirm by remember { mutableStateOf(false) }

    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF141724) else Color(0xFFF3F5FC)
    val cardBorder = if (isDark) Color(0xFF282D42) else Color(0xFFE2E7F5)
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    val animatedProgress by animateFloatAsState(
        targetValue   = (progressPercent / 100f).coerceIn(0.02f, 1f),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label         = "bentoProgressAnim"
    )

    // Shimmer sweep animation for the progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    // Continuous rotation for merging gear/spinner
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mergeRotation"
    )

    // Pulsing alpha for active downloading dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Dynamic byte progress string
    val bytesProgressString = remember(bytesDownloaded, totalBytes) {
        when {
            totalBytes > 0 && bytesDownloaded > 0 ->
                "${formatBytes(bytesDownloaded)} / ${formatBytes(totalBytes)}"
            bytesDownloaded > 0 ->
                "${formatBytes(bytesDownloaded)} downloaded"
            else -> null
        }
    }

    if (showCancelConfirm) {
        BentoAlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = "Stop this download?",
            subtitle = "The active download will be stopped and incomplete files discarded.",
            icon = Icons.Default.Close,
            iconTint = ErrorRed,
            iconBg = Color(0xFFFEE2E2),
            confirmText = "Stop Download",
            confirmColor = ErrorRed,
            dismissText = "Keep Downloading",
            onConfirm = { showCancelConfirm = false; onCancel() },
            onDismiss = { showCancelConfirm = false }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Top Header Row: Status Capsule + Speed Pill + Cancel Action ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Capsule
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (merging) {
                            if (isDark) Color(0xFF2E1C44) else BentoLavenderContainer
                        } else {
                            if (isDark) Color(0xFF162B22) else BentoSageContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (merging) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = BentoPurplePrimary,
                                    modifier = Modifier.size(13.dp).rotate(rotation)
                                )
                                Text(
                                    text = "Merging Audio & Video",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFD8B4FE) else BentoPurplePrimary
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = pulseAlpha))
                                )
                                Text(
                                    text = if (progressPercent > 0) "Downloading Data" else "Connecting…",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF86EFAC) else BentoSageText
                                )
                            }
                        }
                    }

                    // Speed Pill
                    if (!merging && speedFormatted.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isDark) Color(0xFF222638) else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = BentoPurplePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = speedFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                // Sleek Cancel Action
                Surface(
                    onClick = { showCancelConfirm = true },
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF2A1C20) else Color.White,
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF4C2A32) else Color(0xFFFCDADA))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel download",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Middle Section: Thumbnail Preview & Media Info ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Media Thumbnail Preview
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDark) Color(0xFF202336) else Color.White)
                        .border(1.dp, if (isDark) Color(0xFF2C324A) else BentoBorderLight, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaInfo.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = mediaInfo.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BentoPurplePrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Media Details (with multiline support so no awkward clipping!)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaInfo.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoSkyContainer
                        ) {
                            Text(
                                text = mediaInfo.platform.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSkyText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        bytesProgressString?.let {
                            Text(
                                text = it,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            // ── Progress Bar & Percentage Typography ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (merging) "Processing output file…" else "Transfer progress",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoPurplePrimary
                    )
                }

                // Modern glowing gradient capsule progress track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isDark) Color(0xFF222638) else Color(0xFFE2E6F2))
                ) {
                    // Filled progress portion
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF8B5CF6),
                                        BentoPurplePrimary,
                                        Color(0xFF38BDF8)
                                    )
                                )
                            )
                    ) {
                        // Shimmer fluid highlight
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.45f),
                                            Color.Transparent
                                        ),
                                        start = androidx.compose.ui.geometry.Offset(shimmerTranslate, 0f),
                                        end = androidx.compose.ui.geometry.Offset(shimmerTranslate + 240f, 0f)
                                    )
                                )
                        )
                    }
                }
            }

            // ── Connected 4-Stage Pipeline Stepper ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PipelineStep(
                    step = "1",
                    label = "Connect",
                    isComplete = progressPercent > 0,
                    isActive = progressPercent == 0 && !merging,
                    isDark = isDark
                )
                PipelineConnector(isComplete = progressPercent > 0, isDark = isDark)
                PipelineStep(
                    step = "2",
                    label = "Stream",
                    isComplete = progressPercent >= 100 || merging,
                    isActive = progressPercent in 1..99 && !merging,
                    isDark = isDark
                )
                PipelineConnector(isComplete = progressPercent >= 100 || merging, isDark = isDark)
                PipelineStep(
                    step = "3",
                    label = "Merge",
                    isComplete = false,
                    isActive = merging,
                    isDark = isDark
                )
                PipelineConnector(isComplete = false, isDark = isDark)
                PipelineStep(
                    step = "4",
                    label = "Save",
                    isComplete = false,
                    isActive = false,
                    isDark = isDark
                )
            }

            // ── Footer Reassurance: Background Safe Badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Background safe · Download continues if you leave app",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun PipelineStep(
    step: String,
    label: String,
    isComplete: Boolean,
    isActive: Boolean,
    isDark: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isComplete -> BentoPurplePrimary
                        isActive   -> if (isDark) Color(0xFF3B2865) else BentoPurpleContainer
                        else       -> if (isDark) Color(0xFF222638) else Color(0xFFE2E6F2)
                    }
                )
                .then(
                    if (isActive) Modifier.border(1.5.dp, BentoPurplePrimary, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Text(
                    text = step,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isActive -> BentoPurplePrimary
                        else     -> if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
                    }
                )
            }
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive || isComplete) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isActive   -> BentoPurplePrimary
                isComplete -> if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
                else       -> if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight
            }
        )
    }
}

@Composable
private fun RowScope.PipelineConnector(isComplete: Boolean, isDark: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(
                if (isComplete) BentoPurplePrimary
                else if (isDark) Color(0xFF282D42)
                else Color(0xFFE2E6F2)
            )
    )
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
    else                    -> "$bytes B"
}
