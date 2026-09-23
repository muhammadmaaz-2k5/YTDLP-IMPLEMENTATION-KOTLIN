package com.mediasaver.app.domain.repository

import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import kotlinx.coroutines.flow.Flow

/**
 * Runs yt-dlp (and ffmpeg for merging, when needed) on-device.
 *
 * Implemented on Android by a Chaquopy-backed class living in the `androidApp` module
 * (Chaquopy's Gradle plugin only supports the classic `com.android.application`/`com.android.library`
 * DSL, not the `com.android.kotlin.multiplatform.library` plugin this `composeApp` module uses),
 * and consumed here via [com.mediasaver.app.data.di.AppModule].
 */
interface YtDlpEngine {

    /** Runs `yt-dlp --dump-json` (via yt-dlp's Python API) and returns curated metadata + quality options. */
    suspend fun fetchMetadata(url: String): MediaInfo

    /**
     * Downloads [source] for [mediaInfo], re-resolving the format via yt-dlp using
     * [MediaInfo.sourceUrl] rather than any cached CDN URL. Emits [DownloadStatus] updates;
     * the flow is cold — collection starts the download, and cancelling the collector aborts it.
     */
    fun download(mediaInfo: MediaInfo, source: MediaSource): Flow<DownloadStatus>
}
