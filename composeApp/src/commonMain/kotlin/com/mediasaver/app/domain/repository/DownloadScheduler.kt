package com.mediasaver.app.domain.repository

import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import kotlinx.coroutines.flow.Flow

/**
 * Runs a download as durable background work — survives the hosting Activity/composition being
 * destroyed, backgrounding, and (unlike the plain coroutine this replaced) process death, since
 * the Android implementation is backed by WorkManager rather than a scope tied to the UI
 * lifecycle. Also responsible for the progress/complete/failed system notification.
 *
 * There's no queue yet — a new [enqueue] replaces whatever's running (see [AppViewModel]'s
 * UI-level guard against submitting a second download while one is active).
 */
interface DownloadScheduler {
    fun enqueue(jobId: String, mediaInfo: MediaInfo, source: MediaSource)
    fun cancel(jobId: String)
    /** Emits the running/queued job's status; completes (no more emissions) once it reaches a terminal state. */
    fun observeStatus(jobId: String): Flow<DownloadStatus>
}
