package com.mediasaver.app.data.repository

import com.mediasaver.app.data.platform.currentTimeMs
import com.mediasaver.app.data.platform.deleteDownloadedFile
import com.mediasaver.app.data.platform.renameDownloadedFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.repository.DownloadRepository
import com.mediasaver.app.domain.repository.YtDlpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Real [DownloadRepository] backed by on-device yt-dlp (via [engine]).
 *
 * [engine] runs yt-dlp and writes the finished file into a private app temp dir;
 * this class is responsible for publishing that temp file into the public Downloads
 * collection ([fileStore]) and persisting history ([historyStore]) — [engine] itself
 * has no opinion on final storage location.
 */
class YtDlpDownloadRepository(
    private val engine: YtDlpEngine,
    private val fileStore: MediaStoreFileStore,
    private val historyStore: HistoryStore
) : DownloadRepository {

    private val _history = MutableStateFlow<List<DownloadRecord>>(emptyList())

    override suspend fun extractMedia(url: String): List<MediaInfo> {
        val cleanedUrl = com.mediasaver.app.domain.util.SmartUrlEngine.extractAndCleanUrl(url)
            ?: throw IllegalArgumentException("Please enter or paste a valid video or media link.")
        return listOf(engine.fetchMetadata(cleanedUrl))
    }


    override fun download(mediaInfo: MediaInfo, source: MediaSource, destDir: String?): Flow<DownloadStatus> = flow {
        engine.download(mediaInfo, source).collect { status ->
            when (status) {
                is DownloadStatus.Done -> {
                    val displayName = "${mediaInfo.title.sanitizeForFileName()}.${source.ext}"
                    val publishedUri = fileStore.publishToDownloads(
                        tempFile    = File(status.filePath),
                        mimeType    = source.mimeType,
                        displayName = displayName
                    )
                    recordAndEmit(
                        mediaInfo, source, filePath = publishedUri, outcome = DownloadOutcome.COMPLETE
                    )
                    emit(DownloadStatus.Done(publishedUri))
                }
                is DownloadStatus.Failed -> {
                    recordAndEmit(
                        mediaInfo, source, filePath = "", outcome = DownloadOutcome.FAILED,
                        errorMessage = status.reason
                    )
                    emit(status)
                }
                else -> emit(status)
            }
        }
    }

    private suspend fun recordAndEmit(
        mediaInfo: MediaInfo,
        source: MediaSource,
        filePath: String,
        outcome: DownloadOutcome,
        errorMessage: String? = null
    ) {
        val record = DownloadRecord(
            id             = "${mediaInfo.id}_${currentTimeMs()}",
            mediaInfo      = mediaInfo,
            selectedSource = source,
            filePath       = filePath,
            timestampMs    = currentTimeMs(),
            outcome        = outcome,
            errorMessage   = errorMessage
        )
        historyStore.add(record)
        _history.value = listOf(record) + _history.value
    }

    override fun history(): Flow<List<DownloadRecord>> = _history.asStateFlow()

    override suspend fun loadHistory() {
        _history.value = historyStore.load()
    }

    override suspend fun clearHistory() {
        historyStore.clear()
        _history.value = emptyList()
    }

    override suspend fun removeRecord(id: String) {
        historyStore.remove(id)
        _history.value = _history.value.filterNot { it.id == id }
    }

    override suspend fun deleteRecordAndFile(id: String) {
        val record = _history.value.firstOrNull { it.id == id } ?: return
        if (record.outcome == DownloadOutcome.COMPLETE) {
            deleteDownloadedFile(record.filePath)
        }
        historyStore.remove(id)
        _history.value = _history.value.filterNot { it.id == id }
    }

    override suspend fun renameRecord(id: String, newTitle: String): Boolean {
        val record = _history.value.firstOrNull { it.id == id } ?: return false
        if (record.outcome != DownloadOutcome.COMPLETE) return false
        val trimmed = newTitle.trim()
        if (trimmed.isBlank()) return false

        val newDisplayName = "${trimmed.sanitizeForFileName()}.${record.selectedSource.ext}"
        val success = renameDownloadedFile(record.filePath, newDisplayName)
        if (success) {
            val updated = record.copy(mediaInfo = record.mediaInfo.copy(title = trimmed))
            historyStore.update(updated)
            _history.value = _history.value.map { if (it.id == id) updated else it }
        }
        return success
    }
}

private fun String.sanitizeForFileName(): String =
    replace(Regex("[\\\\/:*?\"<>|]"), "_").take(120).ifBlank { "download" }
