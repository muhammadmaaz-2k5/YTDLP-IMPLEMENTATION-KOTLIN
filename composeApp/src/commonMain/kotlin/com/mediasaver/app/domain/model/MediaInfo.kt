package com.mediasaver.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents all metadata and download options for a single piece of media
 * extracted from a URL (Facebook post, Instagram reel, YouTube short, etc.).
 */
@Serializable
data class MediaInfo(
    val id: String,
    val sourceUrl: String,       // Original URL the user pasted
    val title: String,
    val thumbnailUrl: String,
    val type: MediaType,
    val platform: Platform,
    val uploader: String? = null,
    val durationSeconds: Int? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val sources: List<MediaSource> = emptyList()
) {
    val bestSource: MediaSource? get() = sources.firstOrNull()

    @Serializable
    enum class Platform(val displayName: String) {
        FACEBOOK("Facebook"),
        INSTAGRAM("Instagram"),
        YOUTUBE("YouTube"),
        TIKTOK("TikTok"),
        TWITTER("X / Twitter"),
        PINTEREST("Pinterest"),
        GENERIC("Web")
    }
}

