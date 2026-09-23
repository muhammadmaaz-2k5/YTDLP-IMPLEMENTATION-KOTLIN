package com.mediasaver.app.domain.model

/** Represents the current state of a download operation. */
sealed class DownloadStatus {
    object Idle : DownloadStatus()
    object Queued : DownloadStatus()

    data class Downloading(
        /** 0–100 */
        val progressPercent: Int,
        /** Bytes per second, null if unknown */
        val speedBps: Long? = null,
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = 0L
    ) : DownloadStatus() {
        val speedFormatted: String
            get() = speedBps?.let {
                when {
                    it >= 1_048_576 -> "%.1f MB/s".format(it / 1_048_576.0)
                    it >= 1_024     -> "%.0f KB/s".format(it / 1_024.0)
                    else            -> "$it B/s"
                }
            } ?: "…"
    }

    /** yt-dlp finished downloading separate video+audio streams and is now running ffmpeg to merge them. */
    object Merging : DownloadStatus()

    data class Done(val filePath: String) : DownloadStatus()
    data class Failed(val reason: String, val cause: Throwable? = null) : DownloadStatus()
    object Cancelled : DownloadStatus()
}
