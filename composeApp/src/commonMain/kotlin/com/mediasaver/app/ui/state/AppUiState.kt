package com.mediasaver.app.ui.state

import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.MediaInfo

/**
 * Single sealed interface representing every possible UI state of the app.
 * AppViewModel is the only thing that produces these; composables only read them.
 */
sealed interface AppUiState {

    /** Initial state — URL bar visible, empty-state illustration shown. */
    data object Idle : AppUiState

    /** Extraction in progress (network call to fetch video info). */
    data object Loading : AppUiState

    /**
     * Extraction succeeded — show MediaPreviewCard(s).
     * @param items List of [MediaInfo] found (usually 1, sometimes multiple for galleries).
     */
    data class Preview(
        val items: List<MediaInfo>
    ) : AppUiState

    /**
     * Download is in progress.
     * @param progressPercent 0–100
     * @param speedFormatted  Human-readable speed string, e.g. "1.2 MB/s"
     * @param mediaInfo       The item being downloaded (shown in UI)
     */
    data class Downloading(
        val progressPercent: Int,
        val speedFormatted: String,
        val mediaInfo: MediaInfo
    ) : AppUiState

    /**
     * Download completed successfully.
     * @param filePath Absolute path to the saved file.
     * @param mediaInfo The downloaded item.
     */
    data class Success(
        val filePath: String,
        val mediaInfo: MediaInfo
    ) : AppUiState

    /**
     * An error occurred (extraction or download).
     * @param message User-facing error string.
     * @param canRetry Whether a "Retry" button should be shown.
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : AppUiState
}
