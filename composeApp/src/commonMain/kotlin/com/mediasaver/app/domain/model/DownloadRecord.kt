package com.mediasaver.app.domain.model

import kotlinx.serialization.Serializable

/** Whether a [DownloadRecord] finished successfully or failed. */
@Serializable
enum class DownloadOutcome { COMPLETE, FAILED }

/**
 * A download attempt stored in history — either a completed download or a failed one.
 * [outcome]/[errorMessage] default so old `history.json` files (written before these fields
 * existed) still deserialize correctly as [DownloadOutcome.COMPLETE] records.
 */
@Serializable
data class DownloadRecord(
    val id: String,
    val mediaInfo: MediaInfo,
    val selectedSource: MediaSource,
    val filePath: String,
    val timestampMs: Long,
    val outcome: DownloadOutcome = DownloadOutcome.COMPLETE,
    val errorMessage: String? = null
) {
    val fileName: String get() = filePath.substringAfterLast('/')
        .substringAfterLast('\\')
}
