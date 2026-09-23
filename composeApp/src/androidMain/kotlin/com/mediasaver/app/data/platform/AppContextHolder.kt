package com.mediasaver.app.data.platform

import android.content.Context

/**
 * Holds the process-wide Application [Context] for use by expect/actual platform
 * utilities (which take no parameters, per their `commonMain` signatures).
 *
 * Set exactly once, by `androidApp`'s `MediaSaverApp.onCreate()`, before any
 * composable or repository call runs. Safe to hold statically since it's always
 * the Application context, never an Activity context.
 */
object AppContextHolder {
    lateinit var context: Context
}
