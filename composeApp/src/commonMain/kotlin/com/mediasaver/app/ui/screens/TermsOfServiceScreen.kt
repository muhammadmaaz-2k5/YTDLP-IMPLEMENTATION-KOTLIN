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
import com.mediasaver.app.ui.theme.BentoSkyContainer

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

/** Static terms of service screen styled with modern Bento cards. */
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
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
                appName = "Terms of Service",
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
                // Info callout card
                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BentoSkyContainer,
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Fair & Legal Use",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "MediaSaver is designed to download content you own or have explicit rights to save. Please review the terms below.",
                            fontSize = 14.sp,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Section cards
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

