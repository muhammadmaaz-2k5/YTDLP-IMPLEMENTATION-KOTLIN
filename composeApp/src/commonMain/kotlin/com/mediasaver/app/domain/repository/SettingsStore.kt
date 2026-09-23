package com.mediasaver.app.domain.repository

import com.mediasaver.app.domain.model.AppSettings

/** Persists [AppSettings] as a single unit. */
interface SettingsStore {
    suspend fun load(): AppSettings
    suspend fun save(settings: AppSettings)
}
