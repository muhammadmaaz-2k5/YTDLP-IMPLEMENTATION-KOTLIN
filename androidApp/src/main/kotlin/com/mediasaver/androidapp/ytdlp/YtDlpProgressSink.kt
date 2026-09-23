package com.mediasaver.androidapp.ytdlp

/**
 * Passed into Python (`mediasaver_ytdlp.download`) as a plain object — Chaquopy makes any
 * Kotlin/Java object's public methods callable from Python automatically, so this needs no
 * special Chaquopy annotation.
 */
interface YtDlpProgressSink {
    fun onProgress(json: String)
    fun onMerging()
    fun isCancelled(): Boolean
}
