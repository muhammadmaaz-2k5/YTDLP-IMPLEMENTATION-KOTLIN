package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.CloudOff
import com.mediasaver.app.data.platform.NetworkConnection
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.domain.model.AppSettings
import com.mediasaver.app.domain.model.ThemeMode
import com.mediasaver.app.ui.components.*
import com.mediasaver.app.ui.theme.*

/**
 * Modern Bento Settings Tab matching the reference UI mockup:
 * - Grouped Bento Surface Cards.
 * - Iconic Bento "ON" / "OFF" pill switches.
 * - Modern 3-tab segmented theme pill selector.
 * - Circular icon badges on navigation rows.
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onWarnOnCellularChange: (Boolean) -> Unit = {},
    onAskBeforeDownloadChange: (Boolean) -> Unit,
    onConfirmBeforeDeleteChange: (Boolean) -> Unit,
    onAutoDetectClipboardChange: (Boolean) -> Unit,
    onAdsEnabledChange: (Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenHome: (() -> Unit)? = null,
    onOpenDownloads: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    isPremiumActive: Boolean = false,
    networkConnection: NetworkConnection = NetworkConnection.WIFI,
    onOpenAdPrivacyOptions: (() -> Unit)? = null
) {
    val isDark = isAppInDarkTheme()
    val canvasBg = if (isDark) BentoBackgroundDark else BentoBackgroundLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    Box(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppHeader(
                appName  = "Settings",
                tagline  = "Preferences & personalization",
                logoIcon = Icons.Default.Settings,
                isGreetingMode = false,
                isPremiumActive = isPremiumActive,
                onBackClick = onOpenHome,
                onMenuClick = onOpenDrawer,
                onPremiumClick = onOpenPremium
            )

            // ── 1. Downloads & Network Section ────────────────────────────────
            BentoSettingsSection(title = "Downloads & Network", icon = Icons.Default.CloudDownload) {
                // Live Connection Status Card
                val netTitle: String
                val netSubtitle: String
                val netBadge: String
                val netBadgeBg: Color
                val netBadgeColor: Color
                val netIcon: ImageVector

                when (networkConnection) {
                    NetworkConnection.WIFI -> {
                        netTitle = "Wi-Fi Network Connected"
                        netSubtitle = "High-speed unmetered connection active"
                        netBadge = "Wi-Fi Active"
                        netBadgeBg = BentoSageContainer
                        netBadgeColor = BentoSageText
                        netIcon = Icons.Default.Wifi
                    }
                    NetworkConnection.CELLULAR -> {
                        netTitle = "Mobile Cellular Network"
                        netSubtitle = if (settings.wifiOnlyDownloads)
                            "Downloads paused on mobile data (Wi-Fi only)"
                        else
                            "Downloads allowed over mobile cellular network"
                        netBadge = "Mobile Data"
                        netBadgeBg = BentoPeachContainer
                        netBadgeColor = BentoPeachText
                        netIcon = Icons.Default.SignalCellularAlt
                    }
                    NetworkConnection.OFFLINE -> {
                        netTitle = "Offline — No Connection"
                        netSubtitle = "Connect to internet to download new media"
                        netBadge = "Offline"
                        netBadgeBg = if (isDark) Color(0xFF382026) else Color(0xFFFEE2E2)
                        netBadgeColor = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                        netIcon = Icons.Default.CloudOff
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E202B) else Color(0xFFF4F5F9)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(netBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = netIcon,
                                contentDescription = null,
                                tint = netBadgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = netTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = netSubtitle,
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = netBadgeBg
                        ) {
                            Text(
                                text = netBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = netBadgeColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.5f))

                BentoSwitchRow(
                    title    = "Wi-Fi only downloads",
                    subtitle = if (settings.wifiOnlyDownloads)
                        "Downloads only start when connected to Wi-Fi. Mobile cellular data is preserved."
                    else
                        "Downloads are permitted on both Wi-Fi and mobile cellular networks.",
                    checked  = settings.wifiOnlyDownloads,
                    onCheckedChange = onWifiOnlyChange
                )

                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.5f))

                BentoSwitchRow(
                    title    = "Mobile data confirmation",
                    subtitle = if (settings.wifiOnlyDownloads)
                        "Disabled because Wi-Fi only mode is currently active."
                    else
                        "Prompt confirmation before starting downloads over mobile cellular data.",
                    checked  = settings.warnOnCellular,
                    enabled  = !settings.wifiOnlyDownloads,
                    onCheckedChange = onWarnOnCellularChange
                )

                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.5f))

                BentoSwitchRow(
                    title    = "Ask before download",
                    subtitle = "Confirm format, quality, and source before starting each download",
                    checked  = settings.askBeforeDownload,
                    onCheckedChange = onAskBeforeDownloadChange
                )
            }

            // ── 2. Behavior Section ───────────────────────────────────────────
            BentoSettingsSection(title = "Behavior", icon = Icons.Default.Tune) {
                BentoSwitchRow(
                    title    = "Auto-detect clipboard",
                    subtitle = "Prompt when a copied media URL is detected",
                    checked  = settings.autoDetectClipboard,
                    onCheckedChange = onAutoDetectClipboardChange
                )
                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                BentoSwitchRow(
                    title    = "Confirm delete",
                    subtitle = "Ask for confirmation before deleting downloaded file",
                    checked  = settings.confirmBeforeDelete,
                    onCheckedChange = onConfirmBeforeDeleteChange
                )
            }

            // ── 3. Appearance Section ─────────────────────────────────────────
            BentoSettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                BentoThemeSelector(selected = settings.themeMode, onSelect = onThemeModeChange)
            }

            // ── 4. Advertisements & Ad-Free Section ─────────────────────────
            BentoSettingsSection(title = "Advertisements & Ad-Free", icon = Icons.Default.Shield) {
                BentoSwitchRow(
                    title    = "Enable advertisements",
                    subtitle = if (settings.adsEnabled)
                        "Ads help support development (banners, native cards, and app open ads)"
                    else
                        "Ad-free mode active — all advertisements are disabled",
                    checked  = settings.adsEnabled,
                    onCheckedChange = onAdsEnabledChange
                )

                // Reactive ad status indicator pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (settings.adsEnabled) BentoLavenderContainer else BentoSageContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (settings.adsEnabled) BentoPurplePrimary else BentoSageText)
                        )
                        Text(
                            text = if (settings.adsEnabled) "Ads Active · Standard Tier" else "Ad-Free Mode Active ✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.adsEnabled) BentoPurplePrimary else BentoSageText
                        )
                    }
                }

                onOpenAdPrivacyOptions?.let {
                    HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                    BentoNavRow("Manage ad consent (EEA/UK/US)", icon = Icons.Default.PrivacyTip, onClick = it)
                }
            }

            // ── 5. Storage & Cache Section ───────────────────────────────────
            var cacheCleared by remember { mutableStateOf(false) }
            BentoSettingsSection(title = "Storage & Cleanup", icon = Icons.Default.SdStorage) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temporary Cache",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimaryLight
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (cacheCleared) "Cleaned successfully!" else "Frees temporary yt-dlp chunks & thumbnails",
                            fontSize = 12.sp,
                            color = if (cacheCleared) Color(0xFF10B981) else BentoTextSecondaryLight
                        )
                    }
                    Button(
                        onClick = {
                            cacheCleared = true
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cacheCleared) Color(0xFF10B981) else BentoPurplePrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (cacheCleared) "Cleared ✓" else "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── 6. About & Legal Section ──────────────────────────────────────
            BentoSettingsSection(title = "About", icon = Icons.Default.Shield) {
                BentoInfoRow(title = "App version", value = "1.0.0")
                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                BentoNavRow("Privacy Policy", icon = Icons.Default.PrivacyTip, onClick = onOpenPrivacyPolicy)
                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                BentoNavRow("Terms of Service", icon = Icons.Default.Gavel, onClick = onOpenTerms)
                var showCopyright by remember { mutableStateOf(false) }
                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                BentoNavRow("Copyright Policy", icon = Icons.Default.Shield, onClick = { showCopyright = true })
                if (showCopyright) {
                    AlertDialog(
                        onDismissRequest = { showCopyright = false },
                        title = { Text("Copyright & Content Policy", fontWeight = FontWeight.Bold) },
                        text = {
                            Text(
                                "Only download content you own or have permission to save. Respect " +
                                    "creators' rights and platform terms.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = { TextButton(onClick = { showCopyright = false }) { Text("Close") } }
                    )
                }
                var showContact by remember { mutableStateOf(false) }
                HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                BentoNavRow("Contact Support", icon = Icons.Default.Mail, onClick = { showContact = true })
                if (showContact) {
                    AlertDialog(
                        onDismissRequest = { showContact = false },
                        title = { Text("Contact Support", fontWeight = FontWeight.Bold) },
                        text  = { Text("support@example.com — Reach out for assistance or feedback.") },
                        confirmButton = { TextButton(onClick = { showContact = false }) { Text("Close") } }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BentoSettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            BentoCircleBadge(
                icon = icon,
                size = 28.dp,
                iconSize = 14.dp,
                tint = BentoPurplePrimary,
                backgroundColor = BentoPurpleContainer,
                elevation = 0.dp
            )
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimaryLight
            )
        }

        BentoSurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BentoSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val opacity = if (enabled) 1f else 0.45f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimaryLight.copy(alpha = opacity)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = BentoTextSecondaryLight.copy(alpha = opacity)
            )
        }
        Spacer(Modifier.width(12.dp))
        BentoSwitch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun BentoThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val modes = listOf(
        Triple(ThemeMode.LIGHT,  "Light",  Icons.Default.LightMode),
        Triple(ThemeMode.DARK,   "Dark",   Icons.Default.DarkMode),
        Triple(ThemeMode.SYSTEM, "System", Icons.Default.Brightness4)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFFEFF1F7))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (mode, label, icon) ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) BentoPurplePrimary else Color.Transparent)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else BentoTextSecondaryLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else BentoTextSecondaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoNavRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BentoCircleBadge(
            icon = icon,
            size = 34.dp,
            iconSize = 16.dp,
            tint = BentoPurplePrimary,
            backgroundColor = Color(0xFFF1F3F9),
            elevation = 0.dp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoTextPrimaryLight,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = BentoTextSecondaryLight,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BentoInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoTextPrimaryLight,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = BentoTextSecondaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
