package com.mediasaver.androidapp

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.mediasaver.androidapp.ytdlp.ChaquopyYtDlpEngine
import com.mediasaver.app.data.ads.AdsController
import com.mediasaver.app.data.di.AppModule
import com.mediasaver.app.data.download.DownloadNotifier
import com.mediasaver.app.data.download.WorkManagerDownloadScheduler
import com.mediasaver.app.data.platform.AppContextHolder
import com.mediasaver.app.data.repository.HistoryStore
import com.mediasaver.app.data.repository.MediaStoreFileStore
import com.mediasaver.app.data.repository.SharedPrefsOnboardingStore
import com.mediasaver.app.data.repository.SharedPrefsPremiumStore
import com.mediasaver.app.data.repository.SharedPrefsSettingsStore
import com.mediasaver.app.data.repository.YtDlpDownloadRepository
import java.io.File

/**
 * Starts Chaquopy's embedded Python runtime once, and builds the real (non-fake)
 * [AppModule.repository] before any composable or ViewModel can run.
 */
class MediaSaverApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AppContextHolder.context = applicationContext

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        AppModule.repository = YtDlpDownloadRepository(
            engine       = ChaquopyYtDlpEngine(applicationContext),
            fileStore    = MediaStoreFileStore(applicationContext),
            historyStore = HistoryStore(File(filesDir, "history.json").absolutePath)
        )
        AppModule.onboardingStore = SharedPrefsOnboardingStore(applicationContext)
        AppModule.premiumStore = SharedPrefsPremiumStore(applicationContext)
        AppModule.settingsStore = SharedPrefsSettingsStore(applicationContext)
        AppModule.downloadScheduler = WorkManagerDownloadScheduler(applicationContext)
        DownloadNotifier.ensureChannels(applicationContext)

        // Tracks app-foreground/background for the App Open ad — actual consent + SDK init
        // happens later, once an Activity is available (see AppRoot).
        AdsController.setupAppOpenAdLifecycleTracking(this)
    }
}
