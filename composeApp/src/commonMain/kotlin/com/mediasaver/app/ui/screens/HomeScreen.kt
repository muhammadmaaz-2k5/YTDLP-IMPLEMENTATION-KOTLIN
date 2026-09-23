package com.mediasaver.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.readClipboardUrlIfPresent
import com.mediasaver.app.data.platform.showInterstitialAdIfAvailable
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.ui.components.*
import com.mediasaver.app.ui.state.AppUiState
import com.mediasaver.app.ui.state.AppViewModel
import com.mediasaver.app.ui.theme.HeroGradient

/**
 * Home screen — URL input, platform quick-open row, contextual state region
 * (idle hero / preview / downloading / success / error), recently-downloaded
 * preview, and a premium banner.
 *
 * Navigation-agnostic by design: it only calls [onOpenDownloads] / [onOpenPremium] rather than
 * holding a NavController itself, so this commonMain screen has no dependency on any
 * platform-specific navigation library.
 *
 * @param resumeSignal Bumped by the host Activity's `onResume()` (see MainActivity/AppRoot) —
 *                      used to trigger a one-shot clipboard check, never a polling loop.
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenDownloads: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenSettings: () -> Unit,
    resumeSignal: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val url     by viewModel.currentUrl.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var showAbout by remember { mutableStateOf(false) }

    // ── Clipboard "Link detected" prompt ────────────────────────────────────────
    var detectedUrl by remember { mutableStateOf<String?>(null) }
    var lastDismissedUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(resumeSignal) {
        if (!settings.autoDetectClipboard) return@LaunchedEffect
        val clip = readClipboardUrlIfPresent() ?: return@LaunchedEffect
        if (clip != lastDismissedUrl && clip != url) detectedUrl = clip
    }

    // ── "Ask before download" confirmation ──────────────────────────────────────
    var pendingDownload by remember { mutableStateOf<Pair<MediaInfo, MediaSource>?>(null) }
    pendingDownload?.let { (mediaInfo, source) ->
        AlertDialog(
            onDismissRequest = { pendingDownload = null },
            title = { Text("Start this download?") },
            text  = { Text("${mediaInfo.title} · ${source.label}") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDownloadClick(mediaInfo, source); pendingDownload = null }) { Text("Download") }
            },
            dismissButton = { TextButton(onClick = { pendingDownload = null }) { Text("Cancel") } }
        )
    }

    if (showAbout) {
        AboutDialog(
            onDismiss           = { showAbout = false },
            onOpenPrivacyPolicy = { showAbout = false; onOpenPrivacyPolicy() }
        )
    }

    // A plain Box, not a Scaffold(bottomBar = ...) — Scaffold's bottomBar slot reserves hard
    // space and clips content to stop above it, which defeats the point of a floating pill nav
    // bar with rounded corners: content should scroll *behind* it, visible through the
    // transparent margins around the pill, not stop dead at its top edge. The nav bar is
    // layered on top as an overlay instead, and the content gets a tall bottom padding (not a
    // hard clip) purely so real content near the end doesn't sit directly under the opaque pill.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            AppHeader(
                appName  = "MediaSaver",
                tagline  = "Download from any platform instantly",
                logoIcon = Icons.Default.Download,
                actions  = listOf(
                    HeaderAction(
                        icon               = Icons.Default.Info,
                        contentDescription = "About & licenses",
                        onClick            = { showAbout = true }
                    ),
                    HeaderAction(
                        icon               = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium plans",
                        onClick            = onOpenPremium
                    )
                ),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // ── "Link detected" clipboard prompt — a tappable download-icon bubble ─────
            AnimatedVisibility(
                visible = detectedUrl != null,
                enter   = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f, animationSpec = tween(250)),
                exit    = fadeOut(tween(150))
            ) {
                val clip = detectedUrl
                if (clip != null) {
                    Surface(
                        shape     = MaterialTheme.shapes.large,
                        color     = MaterialTheme.colorScheme.primaryContainer,
                        onClick   = {
                            viewModel.onUrlChanged(clip)
                            viewModel.onUrlSubmit(clip)
                            detectedUrl = null
                        }
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier.size(44.dp).clip(CircleShape).background(HeroGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download detected link", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Link detected", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Tap to fetch and download", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { lastDismissedUrl = clip; detectedUrl = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            // ── URL input ─────────────────────────────────────────────────────
            // Disabled (not just spinner-loading) during Downloading: there's no queue yet, so a
            // second submit here would orphan the in-progress download rather than replace it.
            UrlInputBar(
                url         = url,
                onUrlChange = viewModel::onUrlChanged,
                onSubmit    = viewModel::onUrlSubmit,
                isLoading   = uiState is AppUiState.Loading,
                enabled     = uiState !is AppUiState.Downloading
            )
            Text(
                text      = "Reminder: Respect creators' work and intellectual property rights",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // ── Platform quick-open row ──────────────────────────────────────
            SectionDivider(text = "Open Social App to Copy Link")
            PlatformChipsRow()

            // ── State-driven content region ──────────────────────────────────
            AnimatedContent(
                targetState    = uiState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label          = "contentRegion"
            ) { state ->
                when (state) {
                    is AppUiState.Idle -> {
                        val latestComplete = history.firstOrNull { it.outcome == DownloadOutcome.COMPLETE }
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            if (history.isEmpty()) {
                                EmptyState()
                            } else {
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Recently Download",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface,
                                        modifier   = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable(onClick = onOpenDownloads)
                                    ) {
                                        Text("View all", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                if (latestComplete != null) {
                                    RecentDownloadCard(record = latestComplete)
                                } else {
                                    Text(
                                        "No completed downloads yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            PremiumBannerCard(onClick = onOpenPremium)
                            NativeAdCard()
                        }
                    }

                    is AppUiState.Loading -> MediaCardSkeleton()

                    is AppUiState.Preview -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.items.forEach { media ->
                                MediaPreviewCard(
                                    mediaInfo  = media,
                                    onDownload = { source ->
                                        if (settings.askBeforeDownload) pendingDownload = media to source
                                        else viewModel.onDownloadClick(media, source)
                                    }
                                )
                            }
                        }
                    }

                    is AppUiState.Downloading -> DownloadProgressBar(
                        mediaInfo       = state.mediaInfo,
                        progressPercent = state.progressPercent,
                        speedFormatted  = state.speedFormatted,
                        onCancel        = viewModel::onCancel
                    )

                    is AppUiState.Success -> SuccessBanner(
                        filePath = state.filePath,
                        // Natural break point after a completed task — shows the preloaded
                        // interstitial if one's ready, then resets either way (immediately, with
                        // no ad, if consent wasn't given or none loaded in time).
                        onReset  = { showInterstitialAdIfAvailable { viewModel.onReset() } }
                    )

                    is AppUiState.Error -> ErrorBanner(
                        message   = state.message,
                        canRetry  = state.canRetry,
                        onRetry   = viewModel::onRetry,
                        onDismiss = viewModel::onDismissError
                    )
                }
            }

            // Clearance so real content doesn't sit directly under the floating pill + the
            // sticky banner bar now docked beneath it — not a hard stop, just breathing room;
            // the pill's transparent corners still show whatever scrolls underneath as the user
            // scrolls past this point.
            Spacer(Modifier.height(170.dp))
        }

        AppBottomBarWithAd(
            selectedIdx = 0,
            onHome      = { /* already home */ },
            onDownloads = onOpenDownloads,
            onPremium   = onOpenPremium,
            onSettings  = onOpenSettings,
            modifier    = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SectionDivider(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SuccessBanner(filePath: String, onReset: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Download complete",
                tint               = MaterialTheme.colorScheme.secondary,
                modifier           = Modifier.size(48.dp)
            )
            Text("Download Complete!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("Saved to Downloads", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { openFolder(filePath) }, modifier = Modifier.weight(1f)) {
                    Text("Open")
                }
                Button(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("Download Another")
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit, onOpenPrivacyPolicy: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About & Licenses") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "MediaSaver runs yt-dlp and ffmpeg entirely on-device to fetch and download " +
                        "media — there's no backend server of ours involved at any point. Media " +
                        "requests go directly to the site you paste a link from. This app shows " +
                        "ads (Google AdMob) to support development — see Privacy Policy for " +
                        "exactly what that involves.",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text("yt-dlp", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Licensed under the Unlicense (public domain). https://github.com/yt-dlp/yt-dlp", style = MaterialTheme.typography.bodySmall)
                Text("FFmpeg", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    "This build is compiled with --disable-gpl --disable-nonfree and distributed under LGPL-2.1. https://ffmpeg.org",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { TextButton(onClick = onOpenPrivacyPolicy) { Text("Privacy Policy") } }
    )
}
