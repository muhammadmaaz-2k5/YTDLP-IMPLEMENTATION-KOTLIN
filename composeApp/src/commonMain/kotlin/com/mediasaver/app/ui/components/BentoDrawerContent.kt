package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.data.platform.areAdsGloballyEnabled
import com.mediasaver.app.ui.theme.*

/**
 * Modern Bento Drawer Sidebar housing navigation (Home, Downloads, Premium, Settings)
 * and app controls, replacing the persistent bottom bar for an immersive full-screen experience.
 */
@Composable
fun BentoDrawerContent(
    currentRoute: String,
    downloadCount: Int,
    isPremiumActive: Boolean,
    onNavigate: (route: String) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val bgColor = if (isDark) BentoBackgroundDark else BentoBackgroundLight
    val cardColor = if (isDark) BentoCardDark else BentoCardWhite
    val borderColor = if (isDark) BentoBorderDark else BentoBorderLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Block: App Branding + Nav List ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // App Brand Header Card
                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    backgroundColor = cardColor,
                    borderColor = borderColor,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Circular gradient logo
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BentoPurplePrimary, Color(0xFF8E7CFF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Close button
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF232532) else Color(0xFFEFF1F7))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close drawer",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "MediaSaver",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary,
                                    letterSpacing = (-0.3).sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (isPremiumActive) BentoSageContainer else BentoLavenderContainer
                                ) {
                                    Text(
                                        text = if (isPremiumActive) "PRO ACTIVE" else "v1.0.0",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPremiumActive) BentoSageText else BentoPurplePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Smart On-Device Media Saver",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                // Main Navigation Links
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NAVIGATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                    )

                    DrawerNavItem(
                        title = "Home",
                        subtitle = "Downloader & link detector",
                        icon = Icons.Default.Home,
                        isSelected = currentRoute == "home",
                        accentColor = BentoPurplePrimary,
                        containerColor = BentoLavenderContainer,
                        onClick = { onNavigate("home") }
                    )

                    DrawerNavItem(
                        title = "Downloads",
                        subtitle = if (downloadCount > 0) "$downloadCount saved items" else "Offline media library",
                        icon = Icons.Default.Download,
                        isSelected = currentRoute == "downloads",
                        accentColor = BentoSkyText,
                        containerColor = BentoSkyContainer,
                        badge = if (downloadCount > 0) "$downloadCount" else null,
                        onClick = { onNavigate("downloads") }
                    )

                    DrawerNavItem(
                        title = "Premium",
                        subtitle = if (isPremiumActive) "Perks unlocked" else "Highest quality & no watermarks",
                        icon = Icons.Default.WorkspacePremium,
                        isSelected = currentRoute == "premium",
                        accentColor = Color(0xFFD97706),
                        containerColor = BentoPeachContainer,
                        badge = if (isPremiumActive) "ACTIVE" else "PRO",
                        onClick = { onNavigate("premium") }
                    )

                    DrawerNavItem(
                        title = "Settings",
                        subtitle = "Preferences, ad-free & themes",
                        icon = Icons.Default.Settings,
                        isSelected = currentRoute == "settings",
                        accentColor = BentoSageText,
                        containerColor = BentoSageContainer,
                        onClick = { onNavigate("settings") }
                    )
                }

                // Premium / Pro Bento Promo Card
                if (!isPremiumActive) {
                    BentoSurfaceCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("premium") },
                        cornerRadius = 20.dp,
                        backgroundColor = BentoDarkCardBg,
                        borderColor = Color.Transparent,
                        elevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Unlock Full Power",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Highest resolution, no watermarks, and fast downloads.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // ── Bottom Block: Ad Banner / Status + Legal Footnote ─────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // If ads are globally enabled, display sticky ad banner or ad-free status pill
                if (areAdsGloballyEnabled()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        BannerAdSlot(modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoSageContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BentoSageText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ad-Free Mode Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSageText
                            )
                        }
                    }
                }

                HorizontalDivider(color = borderColor.copy(alpha = 0.6f))

                // Legal Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 11.sp,
                        color = textSecondary,
                        modifier = Modifier.clickable(onClick = onOpenPrivacy)
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = textSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Terms of Service",
                        fontSize = 11.sp,
                        color = textSecondary,
                        modifier = Modifier.clickable(onClick = onOpenTerms)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    containerColor: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    val activeBg = if (isSelected) {
        if (isDark) Color(0xFF222433) else Color(0xFFEDEBF9)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = activeBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) containerColor else if (isDark) Color(0xFF1E202B) else Color(0xFFF1F2F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) BentoPurplePrimary else textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) BentoPurplePrimary else textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            badge?.let {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isSelected) BentoPurplePrimary else if (isDark) Color(0xFF2B2D3C) else Color(0xFFE5E7EB)
                ) {
                    Text(
                        text = it,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else textSecondary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
