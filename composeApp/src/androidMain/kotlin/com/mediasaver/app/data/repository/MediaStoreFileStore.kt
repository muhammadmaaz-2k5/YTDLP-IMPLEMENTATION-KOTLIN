package com.mediasaver.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

/**
 * Publishes a finished download (written by yt-dlp into a private app temp dir) into the
 * public Downloads collection, then deletes the temp file.
 *
 * API 29+: uses `MediaStore.Downloads` (no runtime permission needed).
 * API 28 (this app's floor, below MediaStore.Downloads' API 29 floor): falls back to a
 * direct file copy into the public Downloads directory, covered by the `WRITE_EXTERNAL_STORAGE`
 * permission already declared in the manifest with `maxSdkVersion="29"`.
 */
class MediaStoreFileStore(private val context: Context) {

    /** Returns the resulting `content://` URI as a string — API 28's fallback path also returns
     *  a FileProvider `content://` URI (never a raw `file://` one) so it's safe to hand to other
     *  apps via Intent without triggering FileUriExposedException. */
    fun publishToDownloads(tempFile: File, mimeType: String, displayName: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            publishViaMediaStore(tempFile, mimeType, displayName)
        } else {
            publishViaLegacyFile(tempFile, displayName)
        }

    private fun publishViaMediaStore(tempFile: File, mimeType: String, displayName: String): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create MediaStore entry for $displayName")

        resolver.openOutputStream(itemUri)?.use { out ->
            tempFile.inputStream().use { it.copyTo(out) }
        } ?: error("Failed to open output stream for $itemUri")

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(itemUri, values, null, null)

        tempFile.delete()
        return itemUri.toString()
    }

    private fun publishViaLegacyFile(tempFile: File, displayName: String): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir.mkdirs()
        val destFile = File(downloadsDir, displayName)
        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete()
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, destFile).toString()
    }
}
