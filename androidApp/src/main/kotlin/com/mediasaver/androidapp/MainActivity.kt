package com.mediasaver.androidapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mediasaver.androidapp.nav.AppRoot

/**
 * Thin Android launcher activity.
 * Most UI logic lives in the shared composables from :composeApp; navigation wiring lives in
 * [AppRoot] (this module) since it needs the Android-only navigation-compose library.
 */
class MainActivity : ComponentActivity() {

    /**
     * Bumped on every [onResume]. HomeScreen keys a clipboard check off this value (see
     * AppRoot → HomeScreen wiring) so "paste a link, switch back to the app" is detected without
     * polling the clipboard on a timer. Android 10+ blocks background apps from reading the
     * clipboard at all (a deliberate anti-snooping restriction), so on-resume is the earliest
     * point this app is legitimately allowed to check it.
     */
    private var resumeSignal by mutableIntStateOf(0)

    /**
     * The most recently shared URL, from either the launching [Intent] (cold start) or
     * [onNewIntent] (app already running — e.g. singleTask brings it back to front from the
     * Share sheet). A plain `mutableStateOf` field read inside [setContent] re-triggers
     * composition on change even though `setContent` itself isn't re-invoked.
     */
    private var sharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate() per the SplashScreen API contract. postSplashScreenTheme
        // (set on Theme.MediaSaver.Splash in themes.xml) switches the theme back automatically
        // once the first frame of real content below is drawn — no manual dismiss call needed.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedUrl = sharedUrlFrom(intent)
        setContent {
            AppRoot(
                activity     = this,
                darkTheme    = isSystemInDarkTheme(),
                // Material You dynamic color available on Android 12+
                dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                sharedUrl    = sharedUrl,
                resumeSignal = resumeSignal
            )
        }
    }

    override fun onResume() {
        super.onResume()
        resumeSignal++
    }

    /** Handles the Share sheet re-delivering to this already-running Activity (see `singleTask` in the manifest). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUrlFrom(intent)?.let { sharedUrl = it }
    }

    /**
     * Extracts the first http(s) URL from a Share sheet intent (`ACTION_SEND`, `text/plain` or
     * `text/html`). Many apps share a caption *and* a link together (e.g. "Check this out!
     * https://…"), so this pulls the URL out of the surrounding text rather than assuming
     * [Intent.EXTRA_TEXT] is a clean URL by itself.
     */
    private fun sharedUrlFrom(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain" && intent.type != "text/html") return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        return URL_REGEX.find(text)?.value?.trimEnd('.', ',', ';', '!', '?', ')', ']', '}', '\'', '"')
    }

    private companion object {
        val URL_REGEX = Regex("""https?://\S+""")
    }
}
