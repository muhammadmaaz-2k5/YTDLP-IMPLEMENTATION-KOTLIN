package com.mediasaver.app.data.platform

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import com.mediasaver.app.data.ads.AdsController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

actual fun platformName(): String = "Android"

actual fun currentTimeMs(): Long = System.currentTimeMillis()

/** Opens [filePath] (a MediaStore `content://` URI string) with the user's default viewer/player. */
actual fun openFolder(filePath: String) {
    val context = AppContextHolder.context
    val uri = Uri.parse(filePath)
    val mimeType = context.contentResolver.getType(uri) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}

private val dayFormatter = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
    timeZone = TimeZone.getDefault()
}

actual fun dayBucket(epochMs: Long): Long {
    val zone = TimeZone.getDefault()
    return (epochMs + zone.getOffset(epochMs)) / 86_400_000L
}

actual fun formatDayHeader(epochMs: Long): String = dayFormatter.format(java.util.Date(epochMs))

/** Opens [packageName]'s launcher intent, or [webFallbackUrl] in a browser if it's not installed. */
actual fun openApp(packageName: String, webFallbackUrl: String) {
    val context = AppContextHolder.context
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    val intent = launchIntent ?: Intent(Intent.ACTION_VIEW, Uri.parse(webFallbackUrl))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

/** Shares [filePath] (a `content://` or `file://` URI string) via the system share sheet. */
actual fun shareFile(filePath: String, mimeType: String) {
    val context = AppContextHolder.context
    val uri = Uri.parse(filePath)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

actual fun isOnWifi(): Boolean {
    val context = AppContextHolder.context
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
           capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

actual fun isOnCellular(): Boolean {
    val context = AppContextHolder.context
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}

actual fun isOnline(): Boolean {
    val context = AppContextHolder.context
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

actual fun getNetworkConnection(): NetworkConnection {
    val context = AppContextHolder.context
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return NetworkConnection.OFFLINE
    val network = cm.activeNetwork ?: return NetworkConnection.OFFLINE
    val capabilities = cm.getNetworkCapabilities(network) ?: return NetworkConnection.OFFLINE
    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return NetworkConnection.OFFLINE
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkConnection.WIFI
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkConnection.CELLULAR
        else -> NetworkConnection.WIFI
    }
}

actual fun readClipboardUrlIfPresent(): String? = runCatching {
    val context = AppContextHolder.context
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    if (!clipboard.hasPrimaryClip()) return null
    val description = clipboard.primaryClipDescription
    if (description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) != true) return null
    val text = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()?.trim()
    text?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
}.getOrNull()

/** Renames via ContentResolver.update — works for MediaStore URIs; the legacy FileProvider path (API 28) doesn't support it and returns false. */
actual fun renameDownloadedFile(filePath: String, newDisplayName: String): Boolean = runCatching {
    val context = AppContextHolder.context
    val uri = Uri.parse(filePath)
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName)
    }
    context.contentResolver.update(uri, values, null, null) > 0
}.getOrDefault(false)

/** Deletes via ContentResolver.delete — works for both MediaStore URIs and FileProvider URIs (androidx.core.content.FileProvider implements delete()). */
actual fun deleteDownloadedFile(filePath: String): Boolean = runCatching {
    val context = AppContextHolder.context
    val uri = Uri.parse(filePath)
    context.contentResolver.delete(uri, null, null) > 0
}.getOrDefault(false)

actual fun showInterstitialAdIfAvailable(onDismissed: () -> Unit) {
    AdsController.showInterstitialIfAvailable(onDismissed)
}

actual fun showRewardedAd(onRewardEarned: () -> Unit, onAdUnavailable: () -> Unit) {
    AdsController.showRewardedAd(onRewardEarned, onAdUnavailable)
}

actual fun showRewardedInterstitialAd(onRewardEarned: () -> Unit, onAdUnavailable: () -> Unit) {
    AdsController.showRewardedInterstitialAd(onRewardEarned, onAdUnavailable)
}

actual fun setAdsGloballyEnabled(enabled: Boolean) {
    AdsController.setAdsEnabled(enabled)
}

actual fun areAdsGloballyEnabled(): Boolean {
    return AdsController.isAdsEnabled()
}
