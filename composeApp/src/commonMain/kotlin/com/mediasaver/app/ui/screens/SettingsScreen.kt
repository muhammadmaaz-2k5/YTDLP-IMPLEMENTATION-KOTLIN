package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediasaver.app.domain.model.AppSettings
import com.mediasaver.app.domain.model.ThemeMode
import com.mediasaver.app.ui.components.AppBottomBarWithAd
import com.mediasaver.app.ui.components.AppHeader
import com.mediasaver.app.ui.components.HeaderAction

/**
 * Settings tab — Downloads / Behavior / Appearance / About, all backed by [AppSettings] and
 * persisted immediately on every change (no separate "Save" step). Header and section styling
 * match [HomeScreen] / [DownloadsScreen]'s glass-header + card language instead of a plain list.
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onAskBeforeDownloadChange: (Boolean) -> Unit,
    onConfirmBeforeDeleteChange: (Boolean) -> Unit,
    onAutoDetectClipboardChange: (Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPremium: () -> Unit,
    /** Null hides the row entirely — only shown when the UMP consent form actually applies to this user (mainly EEA/UK/US-states). */
    onOpenAdPrivacyOptions: (() -> Unit)? = null
) {
    // See HomeScreen for why this is a Box + overlaid nav bar rather than Scaffold(bottomBar = …).
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeader(
                appName  = "Settings",
                tagline  = "Preferences, behavior & appearance",
                logoIcon = Icons.Default.Settings,
                modifier = Modifier.padding(horizontal = 0.dp)
            )

        SettingsSection(title = "Downloads", icon = Icons.Default.CloudDownload) {
            SwitchRow(
                title    = "Wi-Fi only",
                subtitle = "Don't start downloads on cellular data",
                checked  = settings.wifiOnlyDownloads,
                onCheckedChange = onWifiOnlyChange
            )
            SwitchRow(
                title    = "Ask before starting a download",
                subtitle = "Show a confirmation before each download begins",
                checked  = settings.askBeforeDownload,
                onCheckedChange = onAskBeforeDownloadChange
            )
        }

        SettingsSection(title = "Behavior", icon = Icons.Default.Tune) {
            SwitchRow(
                title    = "Auto-detect clipboard links",
                subtitle = "Prompt when a supported link is copied and you return to the app",
                checked  = settings.autoDetectClipboard,
                onCheckedChange = onAutoDetectClipboardChange
            )
            SwitchRow(
                title    = "Confirm before deleting",
                subtitle = "Ask before removing a downloaded file",
                checked  = settings.confirmBeforeDelete,
                onCheckedChange = onConfirmBeforeDeleteChange
            )
        }

        SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
            ThemeModeSelector(selected = settings.themeMode, onSelect = onThemeModeChange)
        }

        SettingsSection(title = "About", icon = Icons.Default.Shield) {
            InfoRow("App version", "1.0")
            NavRow("Privacy Policy", icon = Icons.Default.PrivacyTip, onClick = onOpenPrivacyPolicy)
            NavRow("Terms of Service", icon = Icons.Default.Gavel, onClick = onOpenTerms)
            onOpenAdPrivacyOptions?.let { NavRow("Manage ad consent", icon = Icons.Default.Shield, onClick = it) }
            var showCopyright by remember { mutableStateOf(false) }
            NavRow("Copyright & Content Policy", onClick = { showCopyright = true })
            if (showCopyright) {
                AlertDialog(
                    onDismissRequest = { showCopyright = false },
                    title = { Text("Copyright & Content Policy") },
                    text = {
                        Text(
                            "Only download content you own or have permission to save. Respect " +
                                "creators' rights and applicable platform terms. This app does not " +
                                "attempt to bypass authentication, access controls, DRM, or private " +
                                "content restrictions, and is not affiliated with, endorsed by, or " +
                                "officially connected to any platform it can fetch media from.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = { TextButton(onClick = { showCopyright = false }) { Text("Close") } }
                )
            }
            var showContact by remember { mutableStateOf(false) }
            NavRow("Contact Support", icon = Icons.Default.Mail, onClick = { showContact = true })
            if (showContact) {
                AlertDialog(
                    onDismissRequest = { showContact = false },
                    title = { Text("Contact Support") },
                    text  = { Text("support@example.com — replace with your real support address before publishing this app.") },
                    confirmButton = { TextButton(onClick = { showContact = false }) { Text("Close") } }
                )
            }
        }

        Spacer(Modifier.height(170.dp))
        }

        AppBottomBarWithAd(
            selectedIdx = 3,
            onHome      = onOpenHome,
            onDownloads = onOpenDownloads,
            onPremium   = onOpenPremium,
            onSettings  = { /* already settings */ },
            modifier    = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier               = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier         = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
            }
            Text(
                title.uppercase(),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                content()
            }
        }
    }
}

/** A 3-segment Light / Dark / System control, replacing a plain radio-button list for a more modern, glanceable picker. */
@Composable
private fun ThemeModeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeOption(
            icon      = Icons.Default.LightMode,
            label     = "Light",
            isSelected = selected == ThemeMode.LIGHT,
            onClick   = { onSelect(ThemeMode.LIGHT) },
            modifier  = Modifier.weight(1f)
        )
        ThemeOption(
            icon      = Icons.Default.DarkMode,
            label     = "Dark",
            isSelected = selected == ThemeMode.DARK,
            onClick   = { onSelect(ThemeMode.DARK) },
            modifier  = Modifier.weight(1f)
        )
        ThemeOption(
            icon      = Icons.Default.Brightness4,
            label     = "System",
            isSelected = selected == ThemeMode.SYSTEM,
            onClick   = { onSelect(ThemeMode.SYSTEM) },
            modifier  = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border   = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NavRow(label: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        icon?.let {
            Box(
                modifier         = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
