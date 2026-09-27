package com.mediasaver.app.domain.util

import com.mediasaver.app.domain.model.MediaInfo

/**
 * High-performance URL parsing, extraction, cleaning, and platform detection engine.
 *
 * Solves real-world user issues:
 * 1. Text containing URLs (e.g. user copies full caption "Watch this: https://instagram.com/reel/xyz?igsh=123 #fun")
 * 2. URLs with messy tracking parameters (?igsh=..., ?si=..., ?fbclid=...) that break or slow yt-dlp down.
 * 3. Human-friendly error translation for common extractor issues (private videos, age gates, deleted media).
 */
object SmartUrlEngine {

    private val URL_REGEX = Regex("""https?://[^\s<>"{}|\\^`]+""", RegexOption.IGNORE_CASE)

    /**
     * Extracts and cleans the first valid HTTP/HTTPS URL from arbitrary input text.
     * Returns null if no valid web URL is found.
     */
    fun extractAndCleanUrl(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return null

        // 1. Find the URL match inside the text
        val rawMatch = URL_REGEX.find(trimmed)?.value ?: return null

        // 2. Strip trailing punctuation often accidentally included in copy-paste
        val cleanedMatch = rawMatch.trimEnd('.', ',', ')', ']', '}', '>', ';', '!', '?', '"', '\'')

        // 3. Clean tracking query parameters
        return cleanTrackingParameters(cleanedMatch)
    }

    /**
     * Strips unwanted telemetry, tracking tokens, and affiliate query params
     * while preserving functional video/post IDs.
     */
    fun cleanTrackingParameters(url: String): String {
        return try {
            val queryIndex = url.indexOf('?')
            if (queryIndex == -1) return url

            val baseUrl = url.substring(0, queryIndex)
            val queryString = url.substring(queryIndex + 1)

            // Tracking parameter keys to drop
            val trackingKeys = setOf(
                "igsh", "si", "fbclid", "sfnsn", "feature", "utm_source",
                "utm_medium", "utm_campaign", "utm_term", "utm_content",
                "app", "ref", "ref_src", "s", "t", "mibextid"
            )

            val params = queryString.split('&')
                .mapNotNull { param ->
                    val eqIdx = param.indexOf('=')
                    val key = if (eqIdx != -1) param.substring(0, eqIdx).lowercase() else param.lowercase()
                    if (key in trackingKeys) null else param
                }

            if (params.isEmpty()) baseUrl else "$baseUrl?${params.joinToString("&")}"
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Detects the media platform early before network extraction.
     */
    fun detectPlatform(url: String): MediaInfo.Platform {
        val lower = url.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> MediaInfo.Platform.YOUTUBE
            lower.contains("instagram.com") -> MediaInfo.Platform.INSTAGRAM
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.gg") -> MediaInfo.Platform.FACEBOOK
            lower.contains("tiktok.com") -> MediaInfo.Platform.TIKTOK
            lower.contains("twitter.com") || lower.contains("x.com") -> MediaInfo.Platform.TWITTER
            lower.contains("pinterest.com") || lower.contains("pin.it") -> MediaInfo.Platform.PINTEREST
            else -> MediaInfo.Platform.GENERIC
        }
    }

    /**
     * Returns a human-friendly platform badge title and tips for the user.
     */
    fun getPlatformInfo(platform: MediaInfo.Platform): PlatformGuide {
        return when (platform) {
            MediaInfo.Platform.YOUTUBE -> PlatformGuide(
                name = "YouTube",
                tagline = "Videos, Shorts, 4K & MP3 audio",
                copyHint = "Tap Share under any video or Short, then Copy Link."
            )
            MediaInfo.Platform.INSTAGRAM -> PlatformGuide(
                name = "Instagram",
                tagline = "Reels, Posts, Carousels & Stories",
                copyHint = "Tap the Share icon on any Reel or Post, then tap Copy Link."
            )
            MediaInfo.Platform.FACEBOOK -> PlatformGuide(
                name = "Facebook",
                tagline = "Watch, Reels, Public posts & HD video",
                copyHint = "Tap the three dots on the post or video and choose Copy Link."
            )
            MediaInfo.Platform.TIKTOK -> PlatformGuide(
                name = "TikTok",
                tagline = "HD videos without watermark",
                copyHint = "Tap the Share arrow, then select Copy Link."
            )
            MediaInfo.Platform.TWITTER -> PlatformGuide(
                name = "X / Twitter",
                tagline = "Video posts, clips & GIFs",
                copyHint = "Tap the Share icon under the post and choose Copy Link."
            )
            MediaInfo.Platform.PINTEREST -> PlatformGuide(
                name = "Pinterest",
                tagline = "Idea pins, clips & DIY videos",
                copyHint = "Tap the Share button on the Pin and select Copy Link."
            )
            MediaInfo.Platform.GENERIC -> PlatformGuide(
                name = "Web Media",
                tagline = "Direct video and audio links",
                copyHint = "Copy any direct media link from your web browser."
            )
        }
    }

    /**
     * Translates raw yt-dlp or network exception strings into user-friendly guidance.
     */
    fun translateErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) return "Unable to process this link. Please verify the URL and try again."

        val lower = rawError.lowercase()
        return when {
            lower.contains("private video") || lower.contains("this video is private") ->
                "This video is set to Private by the creator. MediaSaver only downloads public content."
            lower.contains("login") || lower.contains("sign in") || lower.contains("confirm you're not a bot") ->
                "This content requires account login or verification. Please ensure the link is publicly accessible."
            lower.contains("age-restricted") || lower.contains("age restricted") ->
                "This video is age-restricted and requires account verification on the host platform."
            lower.contains("not available in your country") || lower.contains("geo-restricted") || lower.contains("georestricted") ->
                "This media is region-locked or unavailable in your region."
            lower.contains("video unavailable") || lower.contains("has been removed") || lower.contains("deleted") ->
                "This media has been deleted or is no longer available at this address."
            lower.contains("timeout") || lower.contains("timed out") || lower.contains("unable to connect") || lower.contains("network is unreachable") ->
                "Connection timed out. Please check your internet connection and try again."
            lower.contains("no media found") || lower.contains("unsupported url") || lower.contains("no video formats") ->
                "No downloadable video or audio streams were found at this URL."
            lower.contains("wifi only") || lower.contains("wi-fi only") ->
                "Wi-Fi only is active in Settings, and your device is currently on cellular data."
            else ->
                "Failed to extract media. Please ensure the URL is complete and publicly accessible."
        }
    }

    data class PlatformGuide(
        val name: String,
        val tagline: String,
        val copyHint: String
    )
}
