package com.mediasaver.app.data.di

import com.mediasaver.app.domain.repository.DownloadRepository
import com.mediasaver.app.domain.repository.DownloadScheduler
import com.mediasaver.app.domain.repository.OnboardingStore
import com.mediasaver.app.domain.repository.PremiumStore
import com.mediasaver.app.domain.repository.SettingsStore

/**
 * Application-level DI object.
 *
 * All properties here must be set once, before the first composable is shown, by `androidApp`'s
 * `MediaSaverApp.onCreate()` — they need an Android `Context` (Chaquopy's Python runtime,
 * MediaStore, `filesDir`, SharedPreferences, WorkManager) that isn't available here in commonMain.
 */
object AppModule {
    lateinit var repository: DownloadRepository
    lateinit var onboardingStore: OnboardingStore
    lateinit var premiumStore: PremiumStore
    lateinit var settingsStore: SettingsStore
    lateinit var downloadScheduler: DownloadScheduler
}
