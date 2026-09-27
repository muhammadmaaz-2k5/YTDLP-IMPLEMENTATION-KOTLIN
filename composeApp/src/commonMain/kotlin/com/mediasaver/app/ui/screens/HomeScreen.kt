package com.mediasaver.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.NetworkConnection
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.readClipboardUrlIfPresent
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.data.platform.showInterstitialAdIfAvailable
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.MediaInfo
import com.mediasaver.app.domain.model.MediaSource
import com.mediasaver.app.ui.components.*
import com.mediasaver.app.ui.state.AppUiState
import com.mediasaver.app.ui.state.AppViewModel
import com.mediasaver.app.ui.theme.*

/**
 * Modern Bento Home Screen matching the reference UI mockup:
 * - Minimalist 2-line menu icon & avatar top row.
 * - "Hello," + "MediaSaver" greeting typography.
 * - Elevated Bento URL input pill.
 * - Supported Platforms Bento Grid ("Your Rooms" style).
 * - Weekly Download Activity Chart ("Energy Consumption" style).
 * - Contextual Bento Device & Preview Cards.
 * - Organic Waves Dark Premium Banner Card ("Add New Devices" style).
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenDownloads: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    resumeSignal: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val url     by viewModel.currentUrl.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val temporaryUnlockExpiresAt by viewModel.temporaryUnlockExpiresAt.collectAsState()
    val isPremiumActive = activePlan != null || temporaryUnlockExpiresAt != null
    val networkConnection by viewModel.networkConnection.collectAsState()
    var showAbout by remember { mutableStateOf(false) }

    // ── Clipboard "Link detected" prompt ────────────────────────────────────────
    var detectedUrl by remember { mutableStateOf<String?>(null) }
    var lastDismissedUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(resumeSignal) {
        if (!settings.autoDetectClipboard) return@LaunchedEffect
        val clip = readClipboardUrlIfPresent() ?: return@LaunchedEffect
        if (clip != lastDismissedUrl && clip != url) detectedUrl = clip
    }

    // ── Download confirmation & network guard dialogs ───────────────────────────
    var pendingDownload by remember { mutableStateOf<Pair<MediaInfo, MediaSource>?>(null) }
    var pendingCellularConfirmation by remember { mutableStateOf<Pair<MediaInfo, MediaSource>?>(null) }
    var pendingHighQualityAdConfirmation by remember { mutableStateOf<Pair<MediaInfo, MediaSource>?>(null) }
    var showWifiOnlyBlockedDialog by remember { mutableStateOf(false) }

    val startDownloadFlow: (MediaInfo, MediaSource) -> Unit = { media, source ->
        viewModel.refreshNetworkStatus()
        val currentNet = networkConnection
        when {
            currentNet == NetworkConnection.OFFLINE -> {
                // Let ViewModel error handling provide the offline guidance banner
                viewModel.onDownloadClick(media, source)
            }
            settings.wifiOnlyDownloads && currentNet == NetworkConnection.CELLULAR -> {
                showWifiOnlyBlockedDialog = true
            }
            settings.warnOnCellular && currentNet == NetworkConnection.CELLULAR -> {
                pendingCellularConfirmation = media to source
            }
            // If Free user requesting High Quality format, require viewing interstitial ad!
            !isPremiumActive && source.isHighQuality -> {
                pendingHighQualityAdConfirmation = media to source
            }
            settings.askBeforeDownload -> {
                pendingDownload = media to source
            }
            else -> {
                viewModel.onDownloadClick(media, source)
            }
        }
    }

    pendingDownload?.let { (mediaInfo, source) ->
        BentoDownloadConfirmDialog(
            mediaInfo = mediaInfo,
            source = source,
            onConfirm = {
                viewModel.onDownloadClick(mediaInfo, source)
                pendingDownload = null
            },
            onDismiss = { pendingDownload = null }
        )
    }

    pendingHighQualityAdConfirmation?.let { (mediaInfo, source) ->
        BentoAlertDialog(
            onDismissRequest = { pendingHighQualityAdConfirmation = null },
            title = "Higher Quality Download",
            subtitle = "You selected a premium format (${source.label}). Free users can download higher quality formats by viewing a quick sponsored ad, or unlock Pro for unlimited ad-free downloads!",
            icon = Icons.Default.HighQuality,
            iconTint = BentoPurplePrimary,
            iconBg = BentoPurpleContainer,
            confirmText = "Watch Ad & Download",
            confirmColor = BentoPurplePrimary,
            confirmIcon = Icons.Default.PlayArrow,
            dismissText = "Upgrade to Pro",
            onConfirm = {
                val target = mediaInfo to source
                pendingHighQualityAdConfirmation = null
                showInterstitialAdIfAvailable {
                    if (settings.askBeforeDownload) {
                        pendingDownload = target
                    } else {
                        viewModel.onDownloadClick(mediaInfo, source)
                    }
                }
            },
            onDismiss = {
                pendingHighQualityAdConfirmation = null
                onOpenPremium()
            }
        )
    }

    pendingCellularConfirmation?.let { (mediaInfo, source) ->
        BentoAlertDialog(
            onDismissRequest = { pendingCellularConfirmation = null },
            title = "Download on Mobile Data?",
            subtitle = "You are currently connected to cellular mobile data. Downloading \"${mediaInfo.title}\" (${source.label}) may consume your carrier data allowance.",
            icon = Icons.Default.SignalCellularAlt,
            iconTint = BentoAmberText,
            iconBg = BentoAmberContainer,
            confirmText = "Download Anyway",
            confirmColor = BentoAmberText,
            confirmTextColor = Color.White,
            dismissText = "Cancel",
            onConfirm = {
                val target = mediaInfo to source
                pendingCellularConfirmation = null
                if (!isPremiumActive && source.isHighQuality) {
                    pendingHighQualityAdConfirmation = target
                } else if (settings.askBeforeDownload) {
                    pendingDownload = target
                } else {
                    viewModel.onDownloadClick(mediaInfo, source)
                }
            },
            onDismiss = { pendingCellularConfirmation = null }
        )
    }

    if (showWifiOnlyBlockedDialog) {
        BentoAlertDialog(
            onDismissRequest = { showWifiOnlyBlockedDialog = false },
            title = "Wi-Fi Only Mode Active",
            subtitle = "\"Wi-Fi only downloads\" is enabled in your Settings. To download using mobile cellular data, adjust your network preferences in Settings or connect to a Wi-Fi network.",
            icon = Icons.Default.CloudOff,
            iconTint = BentoRoseText,
            iconBg = BentoRoseContainer,
            confirmText = "Open Settings",
            confirmColor = BentoPurplePrimary,
            dismissText = "Dismiss",
            onConfirm = {
                showWifiOnlyBlockedDialog = false
                onOpenSettings()
            },
            onDismiss = { showWifiOnlyBlockedDialog = false }
        )
    }

    if (showAbout) {
        AboutDialog(
            onDismiss           = { showAbout = false },
            onOpenPrivacyPolicy = { showAbout = false; onOpenPrivacyPolicy() }
        )
    }

    val isDark = isAppInDarkTheme()
    val canvasBg = if (isDark) BentoBackgroundDark else BentoBackgroundLight

    Box(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Modern Bento Header ───────────────────────────────────────────
            AppHeader(
                appName  = "MediaSaver",
                tagline  = "Fast on-device downloads",
                logoIcon = Icons.Default.Download,
                isGreetingMode = true,
                greetingPrefix = "Hello,",
                isPremiumActive = isPremiumActive,
                onMenuClick = onOpenDrawer ?: { showAbout = true },
                onPremiumClick = onOpenPremium,
                actions  = listOf(
                    HeaderAction(
                        icon               = Icons.Default.Info,
                        contentDescription = "About & licenses",
                        onClick            = { showAbout = true }
                    )
                )
            )

            // ── Reusable Offline Widget ───────────────────────────────────────
            AnimatedVisibility(
                visible = networkConnection == NetworkConnection.OFFLINE,
                enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                OfflineWidget(
                    modifier        = Modifier.fillMaxWidth(),
                    isBannerMode    = false,
                    onOpenDownloads = onOpenDownloads,
                    onRetry         = viewModel::refreshNetworkStatus
                )
            }

            // ── "Link detected" clipboard prompt ──────────────────────────────
            AnimatedVisibility(
                visible = detectedUrl != null,
                enter   = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f, animationSpec = tween(250)),
                exit    = fadeOut(tween(150))
            ) {
                val clip = detectedUrl
                if (clip != null) {
                    val detectedPlatform = com.mediasaver.app.domain.util.SmartUrlEngine.detectPlatform(clip)
                    BentoSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        backgroundColor = BentoLavenderContainer,
                        borderColor = Color.Transparent,
                        onClick = {
                            viewModel.onUrlChanged(clip)
                            viewModel.onUrlSubmit(clip)
                            detectedUrl = null
                        }
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BentoCircleBadge(
                                icon = Icons.Default.Download,
                                contentDescription = "Download link",
                                size = 44.dp,
                                iconSize = 22.dp,
                                tint = BentoPurplePrimary,
                                backgroundColor = Color.White,
                                elevation = 2.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${detectedPlatform.displayName} link detected",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoLavenderText
                                )
                                Text(
                                    text = "Tap to fetch, choose quality & download",
                                    fontSize = 12.sp,
                                    color = BentoLavenderSubtext
                                )
                            }
                            IconButton(onClick = { lastDismissedUrl = clip; detectedUrl = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = BentoLavenderText)
                            }
                        }
                    }
                }
            }

            // ── Bento URL Input Pill ──────────────────────────────────────────
            UrlInputBar(
                url         = url,
                onUrlChange = viewModel::onUrlChanged,
                onSubmit    = viewModel::onUrlSubmit,
                isLoading   = uiState is AppUiState.Loading,
                enabled     = uiState !is AppUiState.Downloading
            )

            // ── Quick-Start 3-Step Guide ("How it works") ─────────────────────
            HowItWorksCard()

            // ── State-driven content region ──────────────────────────────────
            AnimatedContent(
                targetState    = uiState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label          = "bentoContentRegion"
            ) { state ->
                when (state) {
                    is AppUiState.Idle -> {
                        val latestComplete = history.firstOrNull { it.outcome == DownloadOutcome.COMPLETE }
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Weekly Activity Bar Chart (matching "Energy Consumption today" card in mockup)
                            BentoWeeklyChart(
                                title = "Download Activity",
                                subtitle = "Weekly on-device processing",
                                metricValue = "${history.size} items"
                            )

                            if (history.isEmpty()) {
                                EmptyState()
                            } else {
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Recently ",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BentoTextPrimaryLight
                                        )
                                        Text(
                                            "Downloaded",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoTextPrimaryLight
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .clickable(onClick = onOpenDownloads)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("View all", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoPurplePrimary)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BentoPurplePrimary, modifier = Modifier.size(16.dp))
                                    }
                                }

                                if (latestComplete != null) {
                                    RecentDownloadCard(record = latestComplete)
                                }
                            }

                            // Dark Organic Waves Premium Banner Card ("Add New Devices" style)
                            PremiumBannerCard(
                                isPremiumActive = isPremiumActive,
                                onClick = onOpenPremium
                            )

                            NativeAdCard()
                        }
                    }

                    is AppUiState.Loading -> MediaCardSkeleton()

                    is AppUiState.Preview -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.items.forEach { media ->
                                MediaPreviewCard(
                                    mediaInfo  = media,
                                    onDownload = { source -> startDownloadFlow(media, source) },
                                    onCancel   = viewModel::onReset
                                )
                            }
                        }
                    }

                    is AppUiState.Downloading -> DownloadProgressBar(
                        mediaInfo       = state.mediaInfo,
                        progressPercent = state.progressPercent,
                        speedFormatted  = state.speedFormatted,
                        bytesDownloaded = state.bytesDownloaded,
                        totalBytes      = state.totalBytes,
                        isMerging       = state.isMerging,
                        onCancel        = viewModel::onCancel
                    )

                    is AppUiState.Success -> BentoSuccessBanner(
                        mediaInfo = state.mediaInfo,
                        filePath  = state.filePath,
                        onReset   = { showInterstitialAdIfAvailable { viewModel.onReset() } }
                    )

                    is AppUiState.Error -> ErrorBanner(
                        message   = state.message,
                        canRetry  = state.canRetry,
                        onRetry   = viewModel::onRetry,
                        onDismiss = viewModel::onDismissError
                    )
                }
            }

            // Bottom padding
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BentoSuccessBanner(
    mediaInfo: MediaInfo,
    filePath: String,
    onReset: () -> Unit
) {
    BentoSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        backgroundColor = BentoSageContainer,
        borderColor = Color.Transparent,
        elevation = 3.dp
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCircleBadge(
                icon = Icons.Default.Check,
                contentDescription = "Success",
                size = 52.dp,
                iconSize = 28.dp,
                tint = BentoSageText,
                backgroundColor = Color.White,
                elevation = 3.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Download Complete!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSageText
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Saved directly to your public Downloads",
                    fontSize = 13.sp,
                    color = BentoSageSubtext
                )
            }

            // Compact Preview of Completed File
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mediaInfo.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = mediaInfo.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaInfo.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = BentoSageText
                        )
                        Text(
                            text = mediaInfo.platform.displayName,
                            fontSize = 12.sp,
                            color = BentoSageSubtext
                        )
                    }
                }
            }

            // Action Buttons: Open, Share, Done (responsive, zero overflow)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { openFolder(filePath) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoSageText)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Open", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = { shareFile(filePath, "video/*") },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BentoSageText
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPurplePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit, onOpenPrivacyPolicy: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About & Licenses", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "MediaSaver runs yt-dlp and ffmpeg entirely on-device to fetch and download " +
                        "media — there's no backend server involved. Media requests go directly to the source.",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text("yt-dlp", fontWeight = FontWeight.Bold)
                Text("Licensed under Unlicense. https://github.com/yt-dlp/yt-dlp", fontSize = 12.sp)
                Text("FFmpeg", fontWeight = FontWeight.Bold)
                Text("Compiled with --disable-gpl --disable-nonfree, distributed under LGPL-2.1.", fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { TextButton(onClick = onOpenPrivacyPolicy) { Text("Privacy Policy") } }
    )
}
