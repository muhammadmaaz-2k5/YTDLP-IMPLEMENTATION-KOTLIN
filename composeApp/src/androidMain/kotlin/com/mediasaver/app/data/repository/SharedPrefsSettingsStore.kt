package com.mediasaver.app.data.repository

import android.content.Context
import com.mediasaver.app.domain.model.AppSettings
import com.mediasaver.app.domain.model.ThemeMode
import com.mediasaver.app.domain.repository.SettingsStore

/** [SettingsStore] backed by Android's default SharedPreferences. */
class SharedPrefsSettingsStore(context: Context) : SettingsStore {

    private val prefs = context.getSharedPreferences("mediasaver_prefs", Context.MODE_PRIVATE)

    override suspend fun load(): AppSettings = AppSettings(
        themeMode           = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "") }
            .getOrDefault(ThemeMode.SYSTEM),
        wifiOnlyDownloads   = prefs.getBoolean(KEY_WIFI_ONLY, false),
        confirmBeforeDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
        askBeforeDownload   = prefs.getBoolean(KEY_ASK_DOWNLOAD, false),
        autoDetectClipboard = prefs.getBoolean(KEY_CLIPBOARD, true)
    )

    override suspend fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_WIFI_ONLY, settings.wifiOnlyDownloads)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmBeforeDelete)
            .putBoolean(KEY_ASK_DOWNLOAD, settings.askBeforeDownload)
            .putBoolean(KEY_CLIPBOARD, settings.autoDetectClipboard)
            .apply()
    }

    private companion object {
        const val KEY_THEME = "settings_theme_mode"
        const val KEY_WIFI_ONLY = "settings_wifi_only"
        const val KEY_CONFIRM_DELETE = "settings_confirm_delete"
        const val KEY_ASK_DOWNLOAD = "settings_ask_download"
        const val KEY_CLIPBOARD = "settings_auto_clipboard"
    }
}
