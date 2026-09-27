package com.mediasaver.app.data.download

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mediasaver.app.data.di.AppModule
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import kotlinx.serialization.json.Json

/**
 * Runs one download to completion as WorkManager background work — this is what makes downloads
 * survive the hosting Activity being destroyed, the app being backgrounded, and process death
 * (WorkManager persists the work request and can re-run it; see [WorkManagerDownloadScheduler]
 * for the "no true resume yet" caveat on a restart after process death).
 *
 * Delegates the actual download to the same [com.mediasaver.app.domain.repository.DownloadRepository]
 * the old ViewModel-owned coroutine used — only *where* it runs changed, not what it does
 * (MediaStore publish + history recording included).
 */
class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val notificationId = DownloadNotifier.notificationIdFor(id)

    override suspend fun doWork(): Result {
        val mediaInfoJson = inputData.getString(KEY_MEDIA_INFO) ?: return Result.failure()
        val sourceJson = inputData.getString(KEY_SOURCE) ?: return Result.failure()
        val mediaInfo = runCatching { Json.decodeFromString<MediaInfo>(mediaInfoJson) }.getOrNull() ?: return Result.failure()
        val source = runCatching { Json.decodeFromString<MediaSource>(sourceJson) }.getOrNull() ?: return Result.failure()

        val settings = runCatching { AppModule.settingsStore.load() }.getOrNull()
        if (settings?.wifiOnlyDownloads == true && !com.mediasaver.app.data.platform.isOnWifi()) {
            DownloadNotifier.showFailed(applicationContext, notificationId, mediaInfo.title, "Download paused: Waiting for Wi-Fi")
            return Result.retry()
        }

        DownloadNotifier.ensureChannels(applicationContext)
        setForeground(foregroundInfo(mediaInfo.title, percent = 0, merging = false))

        var result = Result.failure()
        AppModule.repository.download(mediaInfo, source, destDir = null).collect { status ->
            when (status) {
                is DownloadStatus.Downloading -> {
                    setProgress(workDataOf(
                        KEY_PERCENT to status.progressPercent,
                        KEY_SPEED_BPS to (status.speedBps ?: -1L),
                        KEY_DOWNLOADED_BYTES to status.bytesDownloaded,
                        KEY_TOTAL_BYTES to status.totalBytes
                    ))
                    setForeground(foregroundInfo(mediaInfo.title, status.progressPercent, merging = false, speed = status.speedFormatted))
                }
                is DownloadStatus.Merging -> {
                    setProgress(workDataOf(KEY_PERCENT to 100, KEY_MERGING to true))
                    setForeground(foregroundInfo(mediaInfo.title, percent = 100, merging = true))
                }
                is DownloadStatus.Done -> {
                    DownloadNotifier.showComplete(applicationContext, notificationId, mediaInfo.title, status.filePath, source.mimeType)
                    result = Result.success(workDataOf(KEY_FILE_PATH to status.filePath))
                }
                is DownloadStatus.Failed -> {
                    DownloadNotifier.showFailed(applicationContext, notificationId, mediaInfo.title, status.reason)
                    result = Result.failure(workDataOf(KEY_ERROR to status.reason))
                }
                is DownloadStatus.Cancelled -> {
                    DownloadNotifier.cancelNotification(applicationContext, notificationId)
                    result = Result.failure(workDataOf(KEY_ERROR to "Cancelled"))
                }
                else -> Unit
            }
        }
        return result
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        foregroundInfo(inputData.getString(KEY_TITLE) ?: "Downloading…", percent = 0, merging = false)

    private fun foregroundInfo(title: String, percent: Int, merging: Boolean, speed: String = "…"): ForegroundInfo {
        val notification = DownloadNotifier.progressNotification(
            context         = applicationContext,
            title           = title,
            percent         = percent,
            speedFormatted  = speed,
            merging         = merging,
            cancelIntent    = DownloadNotifier.cancelPendingIntent(applicationContext, id)
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    companion object {
        const val KEY_MEDIA_INFO = "media_info_json"
        const val KEY_SOURCE = "source_json"
        const val KEY_TITLE = "title"
        const val KEY_PERCENT = "percent"
        const val KEY_SPEED_BPS = "speed_bps"
        const val KEY_DOWNLOADED_BYTES = "downloaded_bytes"
        const val KEY_TOTAL_BYTES = "total_bytes"
        const val KEY_MERGING = "merging"
        const val KEY_FILE_PATH = "file_path"
        const val KEY_ERROR = "error"
    }
}
