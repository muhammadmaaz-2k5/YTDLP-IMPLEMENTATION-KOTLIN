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

private data class TermsSection(val heading: String, val body: String)

private val SECTIONS = listOf(
    TermsSection(
        "Your responsibility",
        "Only download content you own or have permission to save. Respect creators' rights and " +
            "the terms of the platform you're downloading from. This app is a tool; how you use " +
            "it is your responsibility."
    ),
    TermsSection(
        "No platform affiliation",
        "MediaSaver is not affiliated with, endorsed by, or officially connected to Facebook, " +
            "Instagram, YouTube, TikTok, X, Pinterest, or any other platform it can fetch media " +
            "from. Platform names and marks shown in the app (e.g. the quick-open row) are used " +
            "only to identify those services, not to claim any partnership."
    ),
    TermsSection(
        "No access-control bypass",
        "This app does not attempt to bypass authentication, access controls, DRM, or private- " +
            "content restrictions. Private, login-required, or otherwise protected content will " +
            "fail to download — that's expected, not a bug to work around."
    ),
    TermsSection(
        "No warranty",
        "This app is provided as-is. Downloads depend on yt-dlp's ability to parse a given site " +
            "at a given time, which can change without notice if the source platform changes its " +
            "own site. We make no guarantee that any particular link will work."
    ),
    TermsSection(
        "Open-source components",
        "This app bundles yt-dlp (Unlicense/public domain) and FFmpeg (LGPL-2.1, built with " +
            "--disable-gpl --disable-nonfree) as on-device tools. See About & Licenses for details."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
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
