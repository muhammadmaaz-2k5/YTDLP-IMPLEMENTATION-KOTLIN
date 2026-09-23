package com.mediasaver.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class PolicySection(val heading: String, val body: String)

// Kept honest and specific to what this app actually does — see YtDlpDownloadRepository,
// HistoryStore and the SharedPreferences-backed stores. Update this alongside any change to
// what data the app reads, stores, or sends.
private val SECTIONS = listOf(
    PolicySection(
        "What this app does",
        "MediaSaver fetches and downloads media using yt-dlp and ffmpeg, both running entirely " +
            "on your device. There is no MediaSaver server. When you paste a link, the app's " +
            "embedded yt-dlp makes requests directly to the site the link points to — the same " +
            "requests your browser would make — and nothing passes through infrastructure we run, " +
            "because none exists."
    ),
    PolicySection(
        "What's stored on your device",
        "Your download history (title, thumbnail URL, platform, file size, saved file location) " +
            "is stored locally as a JSON file in the app's private storage, and is explicitly " +
            "excluded from Android's own device backup — it never leaves your device, full stop, " +
            "not even to your own cloud backup. Your onboarding status, theme, other settings, " +
            "and premium plan selection are stored locally via Android SharedPreferences; unlike " +
            "history, these are included in Android's standard device backup if you have that " +
            "turned on, so a new phone can restore your preferences — that backup goes to your " +
            "own Google account, never to us, since we have no server to send it to."
    ),
    PolicySection(
        "What's never collected",
        "This app has no analytics SDK, no crash reporting service, and no account system. We " +
            "don't know what links you've pasted, what you've downloaded, or that you use this " +
            "app at all — none of that goes anywhere, because we have no server to send it to."
    ),
    PolicySection(
        "Ads (Google AdMob)",
        "This app shows ads via Google AdMob to support development — this is the one exception " +
            "to \"no third party sees anything.\" Google's Mobile Ads SDK can collect device " +
            "identifiers (like the advertising ID), coarse device/app info, and ad " +
            "interaction data to select and measure ads; see Google's own Privacy Policy " +
            "(policies.google.com/privacy) for what Google specifically does with it — we don't " +
            "control that part. Ads never see your download history, pasted links, or downloaded " +
            "files; those stay exactly as described above, untouched by the ad SDK. Where legally " +
            "required (EEA, UK, and similar), you're asked for consent before any ad-related data " +
            "collection happens, and you can change that choice anytime via Settings → " +
            "\"Manage ad consent\" (shown only where it applies)."
    ),
    PolicySection(
        "Thumbnails",
        "Video and image thumbnails shown in previews and history are loaded directly from the " +
            "source platform's own CDN (e.g. Facebook's or Instagram's image servers) over HTTPS, " +
            "the same as loading an image in a browser tab."
    ),
    PolicySection(
        "Data retention & deletion",
        "Download history stays until you remove it — use \"Remove\" on a single item, \"Clear " +
            "all\" on the Downloads screen, or uninstall the app, which deletes everything " +
            "immediately since there's no server copy to delete."
    ),
    PolicySection(
        "Permissions",
        "Internet — required for yt-dlp to fetch media. Network state — lets \"Wi-Fi only\" in " +
            "Settings actually check the connection type. Storage (Android 9 only) — needed to " +
            "save files on versions before scoped storage; on Android 10+, files are saved " +
            "through MediaStore without a storage permission (this app never requests permission " +
            "to read your existing files or media library — only to write new downloads). " +
            "Notifications — download progress/complete/failed status, disable anytime in system " +
            "settings; downloads still work without it, you just won't see a notification. " +
            "Foreground service — lets an active download keep running reliably instead of being " +
            "stopped for running in the background; only active while a download is actually in " +
            "progress."
    ),
    PolicySection(
        "Third-party content",
        "You're responsible for only downloading content you own or have permission to save. " +
            "This app does not attempt to bypass authentication, DRM, or private-content " +
            "restrictions, and gracefully fails on content it can't legitimately access."
    )
)

/** Static, on-device-only privacy policy — see [SECTIONS] for the single source of truth. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Surface(onClick = onBack, modifier = Modifier.padding(8.dp).size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "This app has no server and no account system, so most of this policy is short: " +
                    "we simply don't collect what we never receive. The one exception is ads " +
                    "(Google AdMob) — see that section below for exactly what that involves.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SECTIONS.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(section.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(section.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
