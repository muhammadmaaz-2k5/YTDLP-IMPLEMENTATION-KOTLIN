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

    val isHighQuality: Boolean
        get() {
            val lbl = label.lowercase()
            val res = resolution?.lowercase() ?: ""
            val isHdVideo = res.contains("1080") || res.contains("1440") || res.contains("2160") ||
                    res.contains("4k") || res.contains("2k") ||
                    lbl.contains("1080") || lbl.contains("1440") || lbl.contains("4k") || lbl.contains("1920")
            val isHighFps = (fps ?: 0) >= 50 || lbl.contains("60fps") || lbl.contains("p60")
            val isHighAudio = (lbl.contains("320k") || lbl.contains("256k") || lbl.contains("192k") || lbl.contains("lossless"))
            return isHdVideo || isHighFps || isHighAudio || requiresMerge
        }

    val qualityBadge: String?
        get() {
            val lbl = label.lowercase()
            val res = resolution?.lowercase() ?: ""
            val isAudio = mimeType.startsWith("audio/") || ext in listOf("mp3", "m4a", "wav", "opus") || lbl.contains("audio")

            if (isAudio) {
                return when {
                    lbl.contains("320") -> "320 kbps"
                    lbl.contains("256") -> "256 kbps"
                    lbl.contains("192") -> "192 kbps"
                    lbl.contains("128") || lbl.contains("129") -> "128 kbps"
                    lbl.contains("flac") || lbl.contains("wav") -> "Lossless"
                    else -> "HQ Audio"
                }
            }

            val has60 = (fps ?: 0) >= 50 || lbl.contains("60")
            return when {
                lbl.contains("4k") || res.contains("2160") || lbl.contains("2160") -> if (has60) "4K 60FPS" else "4K UHD"
                lbl.contains("1440") || res.contains("1440") || lbl.contains("2k") -> if (has60) "2K 60FPS" else "2K QHD"
                lbl.contains("1080") || res.contains("1080") || lbl.contains("1920") || res.contains("1920") -> if (has60) "1080p 60FPS" else "1080p FHD"
                lbl.contains("720") || res.contains("720") || lbl.contains("1280") -> if (has60) "720p 60FPS" else "720p HD"
                lbl.contains("480") || res.contains("480") -> "480p SD"
                lbl.contains("360") || res.contains("360") -> "360p"
                has60 -> "60 FPS"
                else -> null
            }
        }
}
