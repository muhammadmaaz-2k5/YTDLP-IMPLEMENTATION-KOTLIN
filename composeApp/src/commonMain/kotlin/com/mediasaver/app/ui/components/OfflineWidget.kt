package com.mediasaver.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.*

/**
 * Reusable Bento Offline Widget supporting both Compact Banner mode
 * and Expansive Card mode. Informs users of offline status and directs
 * them to their local downloaded media.
 */
@Composable
fun OfflineWidget(
    onOpenDownloads: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    isBannerMode: Boolean = false,
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    val cardBg = if (isDark) Color(0xFF241C20) else Color(0xFFFFF6F6)
    val cardBorder = if (isDark) Color(0xFF4C2A32) else Color(0xFFFCDADA)
    val iconContainer = if (isDark) Color(0xFF382026) else Color(0xFFFEE2E2)
    val iconTint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    if (isBannerMode) {
        // ── Compact Banner Mode (Docked / Inline Alert) ────────────────────────
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circle Offline Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title ?: "You're Offline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle ?: "No connection · Local saved media available",
                        fontSize = 11.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onRetry != null) {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF302428) else Color(0xFFFCE7E7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry connection",
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (onOpenDownloads != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onOpenDownloads
                                ),
                            shape = RoundedCornerShape(100.dp),
                            color = BentoPurplePrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Downloads",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ── Expansive Bento Card Mode ──────────────────────────────────────────
        BentoSurfaceCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            backgroundColor = cardBg,
            borderColor = cardBorder,
            elevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row: Icon + Title + Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(iconContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline indicator",
                                tint = iconTint,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title ?: "No Internet Connection",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = subtitle ?: "Offline mode · Wi-Fi & cellular unavailable",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = iconContainer,
                        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = iconTint,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Friendly Guidance Text
                Text(
                    text = "You cannot fetch or download new media while offline, but all your previously saved items in Downloads are fully accessible and ready to play without internet.",
                    fontSize = 13.sp,
                    color = textSecondary,
                    lineHeight = 18.sp
                )

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenDownloads != null) {
                        Button(
                            onClick = onOpenDownloads,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPurplePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Saved Downloads",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (onRetry != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRetry
                                ),
                            shape = RoundedCornerShape(100.dp),
                            color = if (isDark) BentoCardDark else BentoCardWhite,
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Retry",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
