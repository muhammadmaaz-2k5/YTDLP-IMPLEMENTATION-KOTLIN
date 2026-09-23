package com.mediasaver.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import com.mediasaver.app.data.platform.dayBucket
import com.mediasaver.app.data.platform.formatDayHeader
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.ui.components.AppBottomBarWithAd
import com.mediasaver.app.ui.components.AppHeader
import com.mediasaver.app.ui.components.DownloadRow
import com.mediasaver.app.ui.components.HeaderAction
import com.mediasaver.app.ui.components.NativeAdCard
import com.mediasaver.app.ui.theme.HeroGradient

private enum class HistoryFilter(val label: String) {
    ALL("All"), COMPLETE("Complete"), FAILED("Failed")
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

/**
 * Full-screen download history — searchable, filterable (All / Complete / Failed), sortable,
 * grouped by calendar date. Header, stat strip and row cards match the same glass/gradient
 * visual language as [HomeScreen] ([AppHeader], [HeroGradient], rounded card surfaces) instead
 * of a plain default Material top bar.
 *
 * @param records            All past [DownloadRecord]s.
 * @param confirmBeforeDelete From Settings — gates whether [onDeleteConfirmed] fires immediately
 *                            or after an in-screen confirmation dialog.
 * @param onBack              Back arrow → returns to Home.
 * @param onOpenDetail        Tapping a row → the detail screen.
 * @param onDeleteConfirmed   The delete has been confirmed (or no confirmation was required).
 * @param onStartNewDownload  Bottom nav Home tap → back to Home screen.
 * @param onOpenPremium       Bottom nav Premium tap → premium screen.
 * @param onOpenSettings      Bottom nav Settings tap → settings screen.
 */
@Composable
fun DownloadsScreen(
    records: List<DownloadRecord>,
    confirmBeforeDelete: Boolean,
    onBack: () -> Unit,
    onOpenDetail: (DownloadRecord) -> Unit,
    onDeleteConfirmed: (DownloadRecord) -> Unit,
    onStartNewDownload: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DownloadRecord?>(null) }

    pendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title   = { Text("Delete this download?") },
            text    = { Text("This will remove the downloaded file from your device.") },
            confirmButton = {
                TextButton(onClick = { onDeleteConfirmed(record); pendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }

    val query = searchQuery.trim()
    val filtered = records
        .asSequence()
        .filter { record ->
            when (filter) {
                HistoryFilter.ALL      -> true
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

    // See HomeScreen for why this is an outer Box + overlaid nav bar rather than
    // Scaffold(bottomBar = ...) — content needs to scroll behind the floating pill, not stop
    // above it.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppHeader(
                appName  = "Downloads",
                tagline  = if (records.isEmpty()) "Nothing saved yet"
                           else "$completeCount saved · ${formatAggregateBytes(totalBytes)}",
                logoIcon = Icons.Default.Download,
                actions  = listOf(
                    HeaderAction(
                        icon               = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (searchExpanded) "Close search" else "Search downloads",
                        onClick            = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) searchQuery = ""
                        }
                    )
                ),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            AnimatedVisibility(
                visible = searchExpanded,
                enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search title, creator, platform…") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine    = true,
                    shape         = MaterialTheme.shapes.large,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            if (records.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        icon     = Icons.Default.Download,
                        value    = records.size.toString(),
                        label    = "Total",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon     = Icons.Default.CheckCircle,
                        value    = completeCount.toString(),
                        label    = "Complete",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon     = Icons.Default.SdStorage,
                        value    = formatAggregateBytes(totalBytes),
                        label    = "Saved",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status filter chips + sort control
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier               = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryFilter.entries.forEach { f ->
                        val selected = f == filter
                        FilterChip(
                            selected = selected,
                            onClick  = { filter = f },
                            label    = { Text(f.label, style = MaterialTheme.typography.labelMedium) },
                            shape    = MaterialTheme.shapes.extraLarge,
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Box {
                    AssistChip(
                        onClick = { sortMenuExpanded = true },
                        label   = { Text(sortOrder.label, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = { Icon(Icons.Default.SwapVert, contentDescription = "Sort", modifier = Modifier.size(16.dp)) },
                        shape   = MaterialTheme.shapes.extraLarge
                    )
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
                    modifier            = Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier         = Modifier.size(72.dp).clip(CircleShape).background(HeroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (query.isNotBlank()) "No downloads match \"$query\""
                        else "No ${filter.label.lowercase()} downloads yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (sortIsDateBased) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(bottom = 180.dp)
                ) {
                    var itemCounter = 0
                    grouped.forEach { (day, dayRecords) ->
                        item(key = "header_$day") {
                            Text(
                                text     = formatDayHeader(dayRecords.first().timestampMs),
                                style    = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }
                        dayRecords.forEach { record ->
                            item(key = record.id) {
                                DownloadRowCard(
                                    record          = record,
                                    onOpenDetail    = onOpenDetail,
                                    onDeleteRequest = { r -> if (confirmBeforeDelete) pendingDelete = r else onDeleteConfirmed(r) }
                                )
                            }
                            itemCounter++
                            // A native ad every 6 rows — a standard, policy-safe in-feed density
                            // (impressions without turning the list into an ad wall).
                            if (itemCounter % 6 == 0) {
                                item(key = "native_ad_$itemCounter") {
                                    NativeAdCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Size-based sort doesn't group meaningfully by date — flat list instead.
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(bottom = 180.dp, top = 4.dp)
                ) {
                    var itemCounter = 0
                    filtered.forEach { record ->
                        item(key = record.id) {
                            DownloadRowCard(
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

        AppBottomBarWithAd(
            selectedIdx = 1,
            onHome      = onStartNewDownload,
            onDownloads = { /* already downloads */ },
            onPremium   = onOpenPremium,
            onSettings  = onOpenSettings,
            modifier    = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** One of the three at-a-glance numbers shown above the list (Total / Complete / Saved). */
@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** [DownloadRow] wrapped in its own rounded surface card — separates rows visually instead of a flat list. */
@Composable
private fun DownloadRowCard(
    record: DownloadRecord,
    onOpenDetail: (DownloadRecord) -> Unit,
    onDeleteRequest: (DownloadRecord) -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(18.dp),
        color    = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        DownloadRow(
            record          = record,
            onOpenDetail    = onOpenDetail,
            onDeleteRequest = onDeleteRequest,
            modifier        = Modifier.padding(horizontal = 10.dp)
        )
    }
}
