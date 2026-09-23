package com.mediasaver.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType(val label: String, val emoji: String) {
    VIDEO("Video", "🎬"),
    IMAGE("Image", "🖼️"),
    REEL("Reel", "🎞️"),
    SHORT("Short", "⚡"),
    STORY("Story", "⭕"),
    UNKNOWN("Media", "📁")
}
