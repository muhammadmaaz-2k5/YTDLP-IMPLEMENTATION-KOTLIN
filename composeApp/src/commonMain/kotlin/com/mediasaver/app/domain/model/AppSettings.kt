package com.mediasaver.app.domain.model

/** Appearance override; SYSTEM follows the OS light/dark setting. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * All user-configurable app preferences, persisted as a single unit by [com.mediasaver.app.domain.repository.SettingsStore].
 *
 * Every field here is actually enforced somewhere in the app (see call sites of each) —
 * none of these are decorative toggles.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Blocks starting a download while on a metered/cellular connection. Checked in AppViewModel.onDownloadClick. */
    val wifiOnlyDownloads: Boolean = false,
    /** Shows a confirmation dialog before removing a history record. Checked in DownloadsScreen. */
    val confirmBeforeDelete: Boolean = true,
    /** Shows a confirmation dialog before starting a download. Checked in HomeScreen. */
    val askBeforeDownload: Boolean = false,
    /** Enables the "Link detected" clipboard prompt on Home. Checked in HomeScreen. */
    val autoDetectClipboard: Boolean = true
)
