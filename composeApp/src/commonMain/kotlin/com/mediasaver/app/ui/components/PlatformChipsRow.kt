package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.data.platform.openApp
import com.mediasaver.app.ui.theme.*
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Facebook
import compose.icons.simpleicons.Instagram
import compose.icons.simpleicons.Pinterest
import compose.icons.simpleicons.Twitter
import compose.icons.simpleicons.Youtube

data class PlatformData(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val packageName: String,
    val webFallbackUrl: String
)

private val YOUTUBE = PlatformData(
    id = "youtube",
    name = "YouTube",
    subtitle = "4K, 1080p, MP3",
    icon = SimpleIcons.Youtube,
    iconColor = Color(0xFFFF0000),
    packageName = "com.google.android.youtube",
    webFallbackUrl = "https://www.youtube.com"
)

private val INSTAGRAM = PlatformData(
    id = "instagram",
    name = "Instagram",
    subtitle = "Reels & Stories",
    icon = SimpleIcons.Instagram,
    iconColor = Color(0xFFE1306C),
    packageName = "com.instagram.android",
    webFallbackUrl = "https://www.instagram.com"
)

private val FACEBOOK = PlatformData(
    id = "facebook",
    name = "Facebook",
    subtitle = "Public Videos",
    icon = SimpleIcons.Facebook,
    iconColor = Color(0xFF1877F2),
    packageName = "com.facebook.katana",
    webFallbackUrl = "https://www.facebook.com"
)

private val TWITTER = PlatformData(
    id = "twitter",
    name = "X / Twitter",
    subtitle = "Clips & Media",
    icon = SimpleIcons.Twitter,
    iconColor = Color(0xFF111218),
    packageName = "com.twitter.android",
    webFallbackUrl = "https://x.com"
)

private val PINTEREST = PlatformData(
    id = "pinterest",
    name = "Pinterest",
    subtitle = "Pins & Videos",
    icon = SimpleIcons.Pinterest,
    iconColor = Color(0xFFE60023),
    packageName = "com.pinterest",
    webFallbackUrl = "https://www.pinterest.com"
)

/**
 * Bento Grid of supported platforms matching the "Your Rooms" section in the reference mockup:
 * - Left: Featured tall Bento card in pastel sage (like "Living Room").
 * - Right: Two stacked Bento cards in pastel lavender & sky blue (like "Bed Room" & "Office Room").
 */
@Composable
fun PlatformChipsRow(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Supported ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = BentoTextPrimaryLight
                )
                Text(
                    text = "Sites",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoTextPrimaryLight
                )
            }
            Text(
                text = "Tap to open",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoTextSecondaryLight
            )
        }

        // Bento Grid Layout: 1 Featured Tall Card on Left + 2 Stacked Cards on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Featured Tall Bento Card (Sage Green, like "Living Room") ─────
            BentoSurfaceCard(
                modifier = Modifier
                    .weight(1.05f)
                    .height(180.dp),
                cornerRadius = 28.dp,
                backgroundColor = BentoSageContainer,
                borderColor = Color.Transparent,
                onClick = { openApp(YOUTUBE.packageName, YOUTUBE.webFallbackUrl) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Elevated White Circle Badge
                    BentoCircleBadge(
                        icon = YOUTUBE.icon,
                        tint = YOUTUBE.iconColor,
                        size = 46.dp,
                        iconSize = 22.dp,
                        elevation = 3.dp
                    )

                    Column {
                        Text(
                            text = YOUTUBE.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSageText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = YOUTUBE.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoSageSubtext
                        )
                    }
                }
            }

            // ── Stacked Bento Cards on Right (Lavender & Sky) ─────────────────
            Column(
                modifier = Modifier
                    .weight(1.15f)
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stacked Card 1: Instagram (Lavender)
                BentoSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cornerRadius = 24.dp,
                    backgroundColor = BentoLavenderContainer,
                    borderColor = Color.Transparent,
                    onClick = { openApp(INSTAGRAM.packageName, INSTAGRAM.webFallbackUrl) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoCircleBadge(
                            icon = INSTAGRAM.icon,
                            tint = INSTAGRAM.iconColor,
                            size = 38.dp,
                            iconSize = 18.dp,
                            elevation = 2.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = INSTAGRAM.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoLavenderText
                            )
                            Text(
                                text = INSTAGRAM.subtitle,
                                fontSize = 11.sp,
                                color = BentoLavenderSubtext
                            )
                        }
                    }
                }

                // Stacked Card 2: Facebook (Sky Blue)
                BentoSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cornerRadius = 24.dp,
                    backgroundColor = BentoSkyContainer,
                    borderColor = Color.Transparent,
                    onClick = { openApp(FACEBOOK.packageName, FACEBOOK.webFallbackUrl) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoCircleBadge(
                            icon = FACEBOOK.icon,
                            tint = FACEBOOK.iconColor,
                            size = 38.dp,
                            iconSize = 18.dp,
                            elevation = 2.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = FACEBOOK.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSkyText
                            )
                            Text(
                                text = FACEBOOK.subtitle,
                                fontSize = 11.sp,
                                color = BentoSkySubtext
                            )
                        }
                    }
                }
            }
        }

        // Additional Quick Platform Row (Twitter / Pinterest / More)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(TWITTER, PINTEREST).forEach { item ->
                BentoSurfaceCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    cornerRadius = 20.dp,
                    backgroundColor = BentoCardWhite,
                    elevation = 2.dp,
                    onClick = { openApp(item.packageName, item.webFallbackUrl) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCircleBadge(
                            icon = item.icon,
                            tint = item.iconColor,
                            size = 34.dp,
                            iconSize = 16.dp,
                            elevation = 1.dp
                        )
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoTextPrimaryLight
                        )
                    }
                }
            }
        }
    }
}
