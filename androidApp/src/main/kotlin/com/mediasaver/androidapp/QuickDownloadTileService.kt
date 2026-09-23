package com.mediasaver.androidapp

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile ("Quick Download") — reachable from the swipe-down panel on any screen,
 * even the lock screen, regardless of whether MediaSaver is running.
 *
 * Tapping it launches [MainActivity], which becomes the focused foreground app and can then
 * read the clipboard normally through the same on-resume check [HomeScreen] already does — this
 * tile doesn't grant any new clipboard capability, it just removes the "find the launcher icon"
 * friction from getting there. See MainActivity/HomeScreen for why a true always-on background
 * clipboard watcher isn't something Android allows a regular app to do.
 */
class QuickDownloadTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.quick_download_tile_label)
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}
