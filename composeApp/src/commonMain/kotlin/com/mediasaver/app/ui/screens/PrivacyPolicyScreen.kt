package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.components.AppHeader
import com.mediasaver.app.ui.components.BentoSurfaceCard
import com.mediasaver.app.ui.theme.BentoBackgroundLight
import com.mediasaver.app.ui.theme.BentoCardWhite
import com.mediasaver.app.ui.theme.BentoDarkCardBg
import com.mediasaver.app.ui.theme.BentoLavenderContainer
import com.mediasaver.app.ui.theme.BentoPurplePrimary

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

/** Static, on-device-only privacy policy styled with modern Bento cards. */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AppHeader(
                appName = "Privacy Policy",
                isGreetingMode = false,
                onBackClick = onBack
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Highlight hero card
                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BentoLavenderContainer,
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Zero-Server Guarantee",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPurplePrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "This app has no server and no account system: we simply don't collect what we never receive. All downloads process locally on your phone.",
                            fontSize = 14.sp,
                            color = BentoDarkCardBg.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Policy detail cards
                SECTIONS.forEach { section ->
                    BentoSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BentoCardWhite,
                        cornerRadius = 24.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = section.heading,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoDarkCardBg
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = section.body,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

