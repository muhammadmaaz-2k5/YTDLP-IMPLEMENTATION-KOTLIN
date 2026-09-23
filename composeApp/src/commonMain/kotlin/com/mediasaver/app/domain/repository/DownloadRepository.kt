package com.mediasaver.app.domain.repository

import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import kotlinx.coroutines.flow.Flow

/**
 * Single point of truth for all download operations and history.
 *
 * Extraction and download are deliberately separate concerns:
 *  - [extractMedia] → returns metadata + quality options (no file written yet)
 *  - [download]     → starts the actual transfer, emitting progress via Flow
 */
interface DownloadRepository {

    /**
     * Fetches metadata for the given URL.
     * Dispatches to the appropriate [MediaExtractor] internally.
     */
    suspend fun extractMedia(url: String): List<MediaInfo>

    /**
     * Downloads [source] for [mediaInfo], saving to [destDir] (or the platform default if null).
     * Emits [DownloadStatus] updates; final emission is [DownloadStatus.Done] or [DownloadStatus.Failed].
     * The Flow is cold — collection starts the download.
     * Cancel the collecting coroutine (or Job) to abort the download.
     */
    fun download(mediaInfo: MediaInfo, source: MediaSource, destDir: String? = null): Flow<DownloadStatus>

    /** Reactive stream of completed downloads (history). */
    fun history(): Flow<List<DownloadRecord>>

    /**
     * Eagerly loads persisted history from storage into the reactive [history] flow.
     * Safe to call multiple times — implementations should guard against double-loads.
     */
    suspend fun loadHistory() { /* no-op by default */ }

    /** Clears all download history from in-memory cache and any persistent storage. */
    suspend fun clearHistory() { /* no-op by default */ }

    /** Removes a single [DownloadRecord] (by its [DownloadRecord.id]) from history — history only, keeps the file on disk. */
    suspend fun removeRecord(id: String) { /* no-op by default */ }

    /** Deletes the downloaded file itself, then removes its [DownloadRecord] from history. */
    suspend fun deleteRecordAndFile(id: String) { /* no-op by default */ }

    /** Renames the downloaded file and updates the record's title to match. Returns false if the rename failed. */
    suspend fun renameRecord(id: String, newTitle: String): Boolean = false
}
