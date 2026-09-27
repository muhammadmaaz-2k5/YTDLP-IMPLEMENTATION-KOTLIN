package com.mediasaver.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.data.platform.NetworkConnection
import com.mediasaver.app.data.platform.dayBucket
import com.mediasaver.app.data.platform.formatDayHeader
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.ui.components.*
import com.mediasaver.app.ui.theme.*

private enum class HistoryFilter(val label: String) {
    ALL("All"), VIDEOS("Videos"), AUDIO("Audio"), COMPLETE("Complete"), FAILED("Failed")
}

private enum class SortOrder(val label: String) {
    NEWEST("Newest"), OLDEST("Oldest"), LARGEST("Largest"), SMALLEST("Smallest")
}

private fun formatAggregateBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
    else                    -> "$bytes B"
}

@Composable
fun DownloadsScreen(
    records: List<DownloadRecord>,
    confirmBeforeDelete: Boolean,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onOpenDetail: (DownloadRecord) -> Unit,
    onDeleteConfirmed: (DownloadRecord) -> Unit,
    onStartNewDownload: () -> Unit,
    onOpenPremium: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    isPremiumActive: Boolean = false,
    networkConnection: NetworkConnection = NetworkConnection.WIFI,
    onRetryConnection: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null
) {
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DownloadRecord?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    if (showClearAllConfirm) {
        BentoAlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = "Clear all downloads?",
            subtitle = "This will clear your entire download history from the app.",
            icon = Icons.Default.Delete,
            iconTint = ErrorRed,
            iconBg = Color(0xFFFEE2E2),
            confirmText = "Clear All",
            confirmColor = ErrorRed,
            onConfirm = {
                showClearAllConfirm = false
                onClearAll?.invoke()
            },
            onDismiss = { showClearAllConfirm = false }
        )
    }

    pendingDelete?.let { record ->
        BentoAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = "Delete this download?",
            subtitle = "This will remove \"${record.mediaInfo.title}\" from your device storage.",
            icon = Icons.Default.Delete,
            iconTint = ErrorRed,
            iconBg = Color(0xFFFEE2E2),
            confirmText = "Delete",
            confirmColor = ErrorRed,
            onConfirm = {
                onDeleteConfirmed(record)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    val query = searchQuery.trim()
    val filtered = records
        .asSequence()
        .filter { record ->
            when (filter) {
                HistoryFilter.ALL      -> true
                HistoryFilter.VIDEOS   -> record.outcome == DownloadOutcome.COMPLETE &&
                    (record.selectedSource.mimeType.startsWith("video/") || record.selectedSource.ext in listOf("mp4", "webm", "mkv"))
                HistoryFilter.AUDIO    -> record.outcome == DownloadOutcome.COMPLETE &&
                    (record.selectedSource.mimeType.startsWith("audio/") || record.selectedSource.ext in listOf("mp3", "m4a", "opus", "wav"))
                HistoryFilter.COMPLETE -> record.outcome == DownloadOutcome.COMPLETE
                HistoryFilter.FAILED   -> record.outcome == DownloadOutcome.FAILED
            }
        }
        .filter { record ->
            query.isBlank() ||
                record.mediaInfo.title.contains(query, ignoreCase = true) ||
                (record.mediaInfo.uploader?.contains(query, ignoreCase = true) == true) ||
                record.mediaInfo.platform.displayName.contains(query, ignoreCase = true)
        }
        .sortedWith(
            when (sortOrder) {
                SortOrder.NEWEST   -> compareByDescending { it.timestampMs }
                SortOrder.OLDEST   -> compareBy { it.timestampMs }
                SortOrder.LARGEST  -> compareByDescending { it.selectedSource.fileSizeBytes ?: -1L }
                SortOrder.SMALLEST -> compareBy { it.selectedSource.fileSizeBytes ?: Long.MAX_VALUE }
            }
        )
        .toList()


    val grouped = filtered
        .groupBy { dayBucket(it.timestampMs) }
        .toSortedMap(if (sortOrder == SortOrder.OLDEST) compareBy { it } else compareByDescending { it })
    val sortIsDateBased = sortOrder == SortOrder.NEWEST || sortOrder == SortOrder.OLDEST

    val completeCount = records.count { it.outcome == DownloadOutcome.COMPLETE }
    val totalBytes = records.filter { it.outcome == DownloadOutcome.COMPLETE }
        .sumOf { it.selectedSource.fileSizeBytes ?: 0L }
    val videosCount = records.count { it.outcome == DownloadOutcome.COMPLETE && (it.selectedSource.mimeType.startsWith("video/") || it.selectedSource.ext in listOf("mp4", "webm", "mkv")) }
    val audioCount = records.count { it.outcome == DownloadOutcome.COMPLETE && (it.selectedSource.mimeType.startsWith("audio/") || it.selectedSource.ext in listOf("mp3", "m4a", "opus", "wav")) }
    val failedCount = records.count { it.outcome == DownloadOutcome.FAILED }

    val isDark = isAppInDarkTheme()
    val canvasBg = if (isDark) BentoBackgroundDark else BentoBackgroundLight

    Box(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bento Header for Subpage
            AppHeader(
                appName  = "Downloads",
                tagline  = if (records.isEmpty()) "Nothing saved yet"
                           else "$completeCount saved · ${formatAggregateBytes(totalBytes)}",
                logoIcon = Icons.Default.Download,
                isGreetingMode = false,
                isPremiumActive = isPremiumActive,
                onBackClick = onBack,
                onMenuClick = onOpenDrawer,
                onPremiumClick = onOpenPremium,
                actions  = buildList {
                    add(
                        HeaderAction(
                            icon               = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Close search" else "Search downloads",
                            onClick            = {
                                searchExpanded = !searchExpanded
                                if (!searchExpanded) searchQuery = ""
                            }
                        )
                    )
                    if (records.isNotEmpty() && onClearAll != null) {
                        add(
                            HeaderAction(
                                icon               = Icons.Default.Delete,
                                contentDescription = "Clear all downloads",
                                onClick            = { showClearAllConfirm = true }
                            )
                        )
                    }
                }
            )

            // Reusable Compact Offline Banner when disconnected
            AnimatedVisibility(
                visible = networkConnection == NetworkConnection.OFFLINE,
                enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                OfflineWidget(
                    isBannerMode = true,
                    title = "Offline Mode",
                    subtitle = "Showing $completeCount saved files available offline",
                    onRetry = onRetryConnection,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = searchExpanded,
                enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 100.dp,
                    elevation = 2.dp
                ) {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder   = { Text("Search title, creator, platform…", color = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight) },
                        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoPurplePrimary) },
                        singleLine    = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3 Interactive Pastel Bento Stat Chips (Sage, Sky, Lavender)
            if (records.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    BentoStatChip(
                        icon = Icons.Default.Download,
                        value = records.size.toString(),
                        label = "Total",
                        bg = BentoSageContainer,
                        textColor = BentoSageText,
                        subColor = BentoSageSubtext,
                        isSelected = filter == HistoryFilter.ALL,
                        onClick = { filter = HistoryFilter.ALL },
                        modifier = Modifier.weight(1f)
                    )
                    BentoStatChip(
                        icon = Icons.Default.CheckCircle,
                        value = completeCount.toString(),
                        label = "Complete",
                        bg = BentoSkyContainer,
                        textColor = BentoSkyText,
                        subColor = BentoSkySubtext,
                        isSelected = filter == HistoryFilter.COMPLETE,
                        onClick = { filter = HistoryFilter.COMPLETE },
                        modifier = Modifier.weight(1f)
                    )
                    BentoStatChip(
                        icon = Icons.Default.SdStorage,
                        value = formatAggregateBytes(totalBytes),
                        label = "Saved",
                        bg = BentoLavenderContainer,
                        textColor = BentoLavenderText,
                        subColor = BentoLavenderSubtext,
                        isSelected = sortOrder == SortOrder.LARGEST,
                        onClick = { sortOrder = SortOrder.LARGEST },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status filter pill row + sort control (horizontally scrollable, zero overflow)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HistoryFilter.entries.forEach { f ->
                        if (f == HistoryFilter.FAILED && failedCount == 0) return@forEach
                        val selected = f == filter
                        val count = when (f) {
                            HistoryFilter.ALL -> records.size
                            HistoryFilter.VIDEOS -> videosCount
                            HistoryFilter.AUDIO -> audioCount
                            HistoryFilter.COMPLETE -> completeCount
                            HistoryFilter.FAILED -> failedCount
                        }
                        Surface(
                            onClick = { filter = f },
                            shape = RoundedCornerShape(100.dp),
                            color = if (selected) BentoPurplePrimary else if (isDark) BentoCardDark else BentoCardWhite,
                            shadowElevation = if (selected) 2.dp else 1.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = f.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else if (isDark) BentoTextPrimaryDark else BentoTextSecondaryLight
                                )
                                if (count > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (selected) Color.White.copy(alpha = 0.25f) else if (isDark) Color(0xFF282C40) else Color(0xFFE2E8F0)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (selected) Color.White else if (isDark) Color(0xFFCBD5E1) else BentoTextSecondaryLight,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                Box {
                    Surface(
                        onClick = { sortMenuExpanded = true },
                        shape = RoundedCornerShape(100.dp),
                        color = if (isDark) BentoCardDark else BentoCardWhite,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Sort", modifier = Modifier.size(15.dp), tint = BentoPurplePrimary)
                            Spacer(Modifier.width(3.dp))
                            Text(sortOrder.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight)
                        }
                    }
                    DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text    = { Text(order.label) },
                                onClick = { sortOrder = order; sortMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BentoCircleBadge(
                        icon = Icons.Default.Inbox,
                        contentDescription = null,
                        size = 64.dp,
                        iconSize = 30.dp,
                        tint = Color.White,
                        backgroundColor = BentoPurplePrimary,
                        elevation = 3.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (query.isNotBlank()) "No downloads matching \"$query\""
                               else "No downloads in this tab",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimaryLight
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Paste a URL on Home to download any video or audio",
                        fontSize = 13.sp,
                        color = BentoTextSecondaryLight
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onStartNewDownload,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPurplePrimary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download Media", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (sortIsDateBased) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding      = PaddingValues(bottom = 180.dp, top = 4.dp)
                ) {
                    var itemCounter = 0
                    grouped.forEach { (bucket, recordsInBucket) ->
                        val sampleEpoch = recordsInBucket.firstOrNull()?.timestampMs ?: (bucket * 86_400_000L)
                        item(key = "header_$bucket") {
                            Text(
                                text     = formatDayHeader(sampleEpoch),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color    = BentoTextSecondaryLight,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        recordsInBucket.forEach { record ->
                            item(key = record.id) {
                                DownloadRow(
                                    record          = record,
                                    onOpenDetail    = onOpenDetail,
                                    onDeleteRequest = { r -> if (confirmBeforeDelete) pendingDelete = r else onDeleteConfirmed(r) }
                                )
                            }
                            itemCounter++
                            if (itemCounter % 6 == 0) {
                                item(key = "native_ad_$itemCounter") {
                                    NativeAdCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding      = PaddingValues(bottom = 180.dp, top = 4.dp)
                ) {
                    var itemCounter = 0
                    filtered.forEach { record ->
                        item(key = record.id) {
                            DownloadRow(
                                record          = record,
                                onOpenDetail    = onOpenDetail,
                                onDeleteRequest = { r -> if (confirmBeforeDelete) pendingDelete = r else onDeleteConfirmed(r) }
                            )
                        }
                        itemCounter++
                        if (itemCounter % 6 == 0) {
                            item(key = "native_ad_$itemCounter") {
                                NativeAdCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Bento Pastel Stat Chip (Total / Complete / Saved). */
@Composable
private fun BentoStatChip(
    icon: ImageVector,
    value: String,
    label: String,
    bg: Color,
    textColor: Color,
    subColor: Color,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BentoSurfaceCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        backgroundColor = bg,
        borderColor = if (isSelected) textColor.copy(alpha = 0.5f) else Color.Transparent,
        elevation = if (isSelected) 3.dp else 1.dp,
        onClick = onClick
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BentoCircleBadge(
                icon = icon,
                size = 32.dp,
                iconSize = 16.dp,
                tint = textColor,
                elevation = 1.dp
            )
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = subColor)
        }
    }
}

