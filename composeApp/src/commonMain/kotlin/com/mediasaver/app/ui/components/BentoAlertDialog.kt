package com.mediasaver.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.ui.theme.*

/**
 * State-of-the-Art Bento Confirmation Alert Dialog with responsive Dark/Light theme,
 * circular icon badge, rich media preview thumbnail, format chips, and modern pill buttons.
 */
@Composable
fun BentoAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = BentoPurplePrimary,
    iconBg: Color = BentoPurpleContainer,
    confirmText: String = "Confirm",
    confirmColor: Color = BentoPurplePrimary,
    confirmTextColor: Color = Color.White,
    confirmIcon: ImageVector? = null,
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onDismissRequest,
    content: (@Composable () -> Unit)? = null
) {
    val isDark = isAppInDarkTheme()
    val dialogBg = if (isDark) BentoCardDark else Color.White
    val dialogBorder = if (isDark) BentoBorderDark else BentoBorderLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = dialogBg,
            border = BorderStroke(1.dp, dialogBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Icon Badge (if provided)
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isDark) iconBg.copy(alpha = 0.25f) else iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Optional Custom Content Slot
                if (content != null) {
                    content()
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Pill Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onDismiss),
                        shape = RoundedCornerShape(100.dp),
                        color = if (isDark) Color(0xFF222432) else Color(0xFFF1F3F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3144) else BentoBorderLight)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = dismissText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }
                    }

                    // Confirm Pill Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onConfirm),
                        shape = RoundedCornerShape(100.dp),
                        color = confirmColor
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (confirmIcon != null) {
                                Icon(
                                    imageVector = confirmIcon,
                                    contentDescription = null,
                                    tint = confirmTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = confirmText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = confirmTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-End Bento Download Confirmation Dialog showing thumbnail preview,
 * media details, platform badge, resolution tag, and safe download actions.
 */
@Composable
fun BentoDownloadConfirmDialog(
    mediaInfo: MediaInfo,
    source: MediaSource,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1B1D2A) else Color(0xFFF4F6FB)
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    BentoAlertDialog(
        onDismissRequest = onDismiss,
        title = "Start this download?",
        subtitle = "Ready to download and save directly to your device storage",
        icon = Icons.Default.Download,
        iconTint = BentoPurplePrimary,
        iconBg = BentoPurpleContainer,
        confirmText = "Download",
        confirmIcon = Icons.Default.Download,
        confirmColor = BentoPurplePrimary,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    ) {
        // Media Preview Card inside Dialog
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(1.dp, if (isDark) Color(0xFF282B3E) else BentoBorderLight)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Media Thumbnail Preview
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF222432) else Color.White),
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Media Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaInfo.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Platform Tag
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoLavenderContainer
                        ) {
                            Text(
                                text = mediaInfo.platform.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoLavenderText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Quality Chip
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoSkyContainer
                        ) {
                            Text(
                                text = source.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSkyText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // File size if known
                        source.fileSizeBytes?.let { bytes ->
                            Text(
                                text = formatDialogBytes(bytes),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDialogBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
    else                    -> "$bytes B"
}
