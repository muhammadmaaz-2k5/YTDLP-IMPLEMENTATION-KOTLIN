package com.mediasaver.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A single quality option for a piece of media, backed by a yt-dlp format selector
 * (e.g. "137+140" for a video-only stream that needs audio merged in, or a plain
 * format id like "22" when video+audio are already combined).
 *
 * [url] is informational only (for display/debugging) — actual downloads always
 * re-resolve through yt-dlp using [MediaInfo.sourceUrl] + [formatId], since CDN
 * URLs captured at extraction time are typically short-lived/signed and may have
 * expired by the time the user clicks Download.
 */
@Serializable
data class MediaSource(
    val label: String,
    val url: String,
    val mimeType: String = "video/mp4",
    val fileSizeBytes: Long? = null,
    /** yt-dlp `-f` format selector string, e.g. "137+140" or "22". */
    val formatId: String = "best",
    val ext: String = "mp4",
    val resolution: String? = null,
    val fps: Int? = null,
    /** True if [formatId] selects separate video+audio streams that yt-dlp/ffmpeg must merge. */
    val requiresMerge: Boolean = false
) {
    val fileSizeFormatted: String?
        get() = fileSizeBytes?.let {
            when {
                it >= 1_073_741_824 -> "%.1f GB".format(it / 1_073_741_824.0)
                it >= 1_048_576     -> "%.1f MB".format(it / 1_048_576.0)
                it >= 1_024         -> "%.0f KB".format(it / 1_024.0)
                else                -> "$it B"
            }
        }
}
