package com.mediasaver.androidapp.ytdlp

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.mediasaver.app.domain.model.DownloadStatus
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.domain.repository.YtDlpEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runs yt-dlp as embedded Python (via Chaquopy) and merges with the bundled static
 * ffmpeg binary (`jniLibs/arm64-v8a/libffmpeg.so` + `libffprobe.so`, exposed at
 * [Context.getApplicationInfo]'s `nativeLibraryDir` — packaged there automatically because
 * AAPT treats jniLibs as native libraries, which get exec permission for free, unlike a file
 * merely extracted to `filesDir`).
 *
 * yt-dlp identifies the ffmpeg/ffprobe binaries by filename ("ffmpeg"/"ffprobe"), but Android
 * requires them to be packaged as "lib*.so" to get exec permission — so [ffmpegDir] creates
 * correctly-named symlinks in a writable directory pointing at the real `nativeLibraryDir`
 * files. `execve()` resolves a symlink to its target before checking exec permission, so the
 * symlink is executable even though the directory holding it (`filesDir`) itself is not.
 */
class ChaquopyYtDlpEngine(private val context: Context) : YtDlpEngine {

    private val module: PyObject by lazy { Python.getInstance().getModule("mediasaver_ytdlp") }

    /** Directory containing `ffmpeg`/`ffprobe` symlinks — passed to yt-dlp's `ffmpeg_location`. */
    private val ffmpegDir: String by lazy { ensureFfmpegSymlinks().absolutePath }

    private fun ensureFfmpegSymlinks(): File {
        val binDir = File(context.filesDir, "bin").apply { mkdirs() }
        val nativeDir = context.applicationInfo.nativeLibraryDir
        linkIfNeeded(File(binDir, "ffmpeg").toPath(), File(nativeDir, "libffmpeg.so"))
        linkIfNeeded(File(binDir, "ffprobe").toPath(), File(nativeDir, "libffprobe.so"))
        return binDir
    }

    private fun linkIfNeeded(link: Path, target: File) {
        if (!target.exists()) return
        val currentTarget = runCatching { Files.readSymbolicLink(link) }.getOrNull()
        if (currentTarget?.toString() == target.absolutePath) return
        Files.deleteIfExists(link)
        Files.createSymbolicLink(link, target.toPath())
    }

    override suspend fun fetchMetadata(url: String): MediaInfo = withContext(Dispatchers.IO) {
        val raw = module.callAttr("fetch_metadata", url).toString()
        parseMetadata(JSONObject(raw), url)
    }

    override fun download(mediaInfo: MediaInfo, source: MediaSource): Flow<DownloadStatus> = callbackFlow {
        val cancelled = AtomicBoolean(false)
        val jobDir = File(context.cacheDir, "ytdlp_downloads/${UUID.randomUUID()}").apply { mkdirs() }
        val outTemplate = File(jobDir, "%(title).200B.%(ext)s").absolutePath

        val sink = object : YtDlpProgressSink {
            override fun onProgress(json: String) {
                val d = JSONObject(json)
                val downloaded = d.optLong("downloaded_bytes", 0L)
                val total = d.optLong("total_bytes", 0L)
                val percent = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 100) else 0
                val speed = if (d.isNull("speed")) null else d.optDouble("speed", 0.0).toLong()
                trySend(DownloadStatus.Downloading(percent, speed, downloaded, total))
            }

            override fun onMerging() {
                trySend(DownloadStatus.Merging)
            }

            override fun isCancelled(): Boolean = cancelled.get()
        }

        trySend(DownloadStatus.Queued)

        // The Chaquopy call below is a genuinely blocking (non-suspending) native call, so it
        // must run on its own thread rather than directly in this coroutine: otherwise this
        // producer body would never reach `awaitClose`, and cancellation could never set
        // `cancelled` while a download is in progress (yt-dlp's hook only sees it via polling).
        val thread = Thread {
            try {
                module.callAttr(
                    "download",
                    mediaInfo.sourceUrl,
                    source.formatId,
                    outTemplate,
                    ffmpegDir,
                    sink
                )
                val finalFile = jobDir.listFiles()
                    ?.filterNot { it.name.endsWith(".part") || it.name.endsWith(".ytdl") }
                    ?.maxByOrNull { it.length() }
                if (finalFile != null) {
                    trySend(DownloadStatus.Done(finalFile.absolutePath))
                } else {
                    trySend(DownloadStatus.Failed("yt-dlp finished but produced no output file"))
                }
            } catch (e: Throwable) {
                if (cancelled.get()) {
                    trySend(DownloadStatus.Cancelled)
                } else {
                    trySend(DownloadStatus.Failed(e.message ?: "Download failed", e as? Exception))
                }
            } finally {
                close()
            }
        }
        thread.isDaemon = true
        thread.start()

        awaitClose { cancelled.set(true) }
    }.flowOn(Dispatchers.IO)

    private fun parseMetadata(json: JSONObject, sourceUrl: String): MediaInfo {
        val formatsArray = json.optJSONArray("formats") ?: JSONArray()
        val sources = (0 until formatsArray.length()).map { i ->
            val f = formatsArray.getJSONObject(i)
            MediaSource(
                label         = f.optString("label", "Unknown"),
                url           = "",
                mimeType      = mimeTypeForExt(f.optString("ext", "mp4")),
                fileSizeBytes = if (f.isNull("filesize")) null else f.optLong("filesize"),
                formatId      = f.optString("format_selector", "best"),
                ext           = f.optString("ext", "mp4"),
                resolution    = if (f.isNull("resolution")) null else f.optString("resolution"),
                fps           = if (f.isNull("fps")) null else f.optInt("fps"),
                requiresMerge = f.optBoolean("requires_merge", false)
            )
        }
        val tagsArray = json.optJSONArray("tags") ?: JSONArray()
        val tagsList = (0 until tagsArray.length()).mapNotNull { i -> tagsArray.optString(i).takeIf { it.isNotBlank() } }
        val description = if (json.isNull("description")) null else json.optString("description").takeIf { it.isNotBlank() }

        return MediaInfo(
            id              = json.optString("id", sourceUrl),
            sourceUrl       = sourceUrl,
            title           = json.optString("title", "Untitled"),
            thumbnailUrl    = json.optString("thumbnail", ""),
            type            = if (sources.any { it.mimeType.startsWith("image/") && it.formatId != "thumbnail" }) MediaType.IMAGE else MediaType.VIDEO,
            platform        = platformFor(json.optString("extractor_key", "")),
            uploader        = if (json.isNull("uploader")) null else json.optString("uploader").takeIf { it.isNotBlank() },
            durationSeconds = if (json.isNull("duration")) null else json.optInt("duration"),
            description     = description,
            tags            = tagsList,
            sources         = sources
        )
    }

    private fun mimeTypeForExt(ext: String): String = when (ext.lowercase()) {
        "mp4", "m4v"  -> "video/mp4"
        "webm"        -> "video/webm"
        "mkv"         -> "video/x-matroska"
        "m4a"         -> "audio/mp4"
        "mp3"         -> "audio/mpeg"
        "opus"        -> "audio/opus"
        "jpg", "jpeg" -> "image/jpeg"
        "png"         -> "image/png"
        "webp"        -> "image/webp"
        else          -> "application/octet-stream"
    }

    private fun platformFor(extractorKey: String): MediaInfo.Platform = when {
        extractorKey.contains("facebook", ignoreCase = true)  -> MediaInfo.Platform.FACEBOOK
        extractorKey.contains("instagram", ignoreCase = true) -> MediaInfo.Platform.INSTAGRAM
        extractorKey.contains("youtube", ignoreCase = true)   -> MediaInfo.Platform.YOUTUBE
        extractorKey.contains("tiktok", ignoreCase = true)    -> MediaInfo.Platform.TIKTOK
        extractorKey.contains("twitter", ignoreCase = true) || extractorKey.contains("x", ignoreCase = true) -> MediaInfo.Platform.TWITTER
        extractorKey.contains("pinterest", ignoreCase = true) -> MediaInfo.Platform.PINTEREST
        else -> MediaInfo.Platform.GENERIC
    }
}

