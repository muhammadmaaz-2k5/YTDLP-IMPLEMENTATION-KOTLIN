package com.mediasaver.app.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import java.util.UUID

/**
 * Notification channels + builders for the download worker. Two channels, matching how Android
 * users expect this to behave: silent/ongoing while progressing, normal priority for a one-shot
 * result — using one channel for both would either spam a sound on every percent tick or bury
 * the completion notification.
 */
object DownloadNotifier {
    private const val CHANNEL_PROGRESS = "download_progress"
    private const val CHANNEL_STATUS = "download_status"

    // `composeApp`'s KMP android-library target doesn't expose a usable generated R class for
    // its own androidMain/res (unlike a classic com.android.library module — androidApp's own
    // drawables work fine, but this module can't reach them, and can't reach its own either),
    // so this uses the framework's built-in download icon instead of a custom drawable resource.
    private val ICON = android.R.drawable.stat_sys_download

    /** Stable per-job notification id, derived from the job's WorkManager UUID. */
    fun notificationIdFor(workId: UUID): Int = workId.hashCode()

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PROGRESS, "Download progress", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Ongoing download progress — silent, updates in place"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_STATUS, "Download results", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Download completed or failed"
            }
        )
    }

    fun progressNotification(
        context: Context,
        title: String,
        percent: Int,
        speedFormatted: String,
        merging: Boolean,
        cancelIntent: PendingIntent
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(ICON)
            .setContentTitle(title)
            .setContentText(if (merging) "Merging…" else "$percent% · $speedFormatted")
            .setProgress(100, percent, merging)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Cancel", cancelIntent)
            .build()

    fun showComplete(context: Context, notificationId: Int, title: String, filePath: String, mimeType: String) {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(filePath), mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(ICON)
            .setContentTitle("Download complete")
            .setContentText(title)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).cancel(notificationId)
        runCatching { NotificationManagerCompat.from(context).notify(notificationId, notification) }
    }

    fun showFailed(context: Context, notificationId: Int, title: String, reason: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(ICON)
            .setContentTitle("Download failed")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n${reason.take(200)}"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).cancel(notificationId)
        runCatching { NotificationManagerCompat.from(context).notify(notificationId, notification) }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /** A ready-made PendingIntent that cancels this specific WorkManager job — for the notification's Cancel action. */
    fun cancelPendingIntent(context: Context, workId: UUID): PendingIntent =
        WorkManager.getInstance(context).createCancelPendingIntent(workId)
}
