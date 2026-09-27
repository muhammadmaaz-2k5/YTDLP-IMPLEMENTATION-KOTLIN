package com.mediasaver.app.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mediasaver.app.data.di.AppModule
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.repository.DownloadScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * [DownloadScheduler] backed by WorkManager. There's no queue yet — every job uses the same
 * unique work name, so a new [enqueue] naturally replaces whatever's currently running/queued
 * (see [ExistingWorkPolicy.REPLACE]), matching the app's current "one download at a time"
 * architecture instead of silently allowing a second one to run unmanaged.
 *
 * [jobId] is accepted for API symmetry with a future real queue but isn't used to key WorkManager
 * state yet — it's folded into the single [UNIQUE_WORK_NAME] slot.
 */
class WorkManagerDownloadScheduler(private val context: Context) : DownloadScheduler {

    private val workManager get() = WorkManager.getInstance(context)

    override fun enqueue(jobId: String, mediaInfo: MediaInfo, source: MediaSource) {
        val isWifiOnly = runBlocking {
            runCatching { AppModule.settingsStore.load().wifiOnlyDownloads }.getOrDefault(false)
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_MEDIA_INFO to Json.encodeToString(mediaInfo),
                    DownloadWorker.KEY_SOURCE to Json.encodeToString(source),
                    DownloadWorker.KEY_TITLE to mediaInfo.title
                )
            )
            .build()
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(jobId: String) {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    override fun observeStatus(jobId: String): Flow<DownloadStatus> =
        workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .mapNotNull { infos -> infos.firstOrNull()?.toDownloadStatus() }

    private fun WorkInfo.toDownloadStatus(): DownloadStatus? = when (state) {
        WorkInfo.State.ENQUEUED -> DownloadStatus.Queued
        WorkInfo.State.RUNNING -> {
            if (progress.getBoolean(DownloadWorker.KEY_MERGING, false)) {
                DownloadStatus.Merging
            } else {
                val percent = progress.getInt(DownloadWorker.KEY_PERCENT, 0)
                val speedBps = progress.getLong(DownloadWorker.KEY_SPEED_BPS, -1L).takeIf { it >= 0 }
                val downloaded = progress.getLong(DownloadWorker.KEY_DOWNLOADED_BYTES, 0L)
                val total = progress.getLong(DownloadWorker.KEY_TOTAL_BYTES, 0L)
                DownloadStatus.Downloading(
                    progressPercent = percent,
                    speedBps = speedBps,
                    bytesDownloaded = downloaded,
                    totalBytes = total
                )
            }
        }
        WorkInfo.State.SUCCEEDED -> DownloadStatus.Done(outputData.getString(DownloadWorker.KEY_FILE_PATH) ?: "")
        WorkInfo.State.FAILED -> DownloadStatus.Failed(outputData.getString(DownloadWorker.KEY_ERROR) ?: "Download failed")
        WorkInfo.State.CANCELLED -> DownloadStatus.Cancelled
        WorkInfo.State.BLOCKED -> null
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "single_download_slot"
    }
}
