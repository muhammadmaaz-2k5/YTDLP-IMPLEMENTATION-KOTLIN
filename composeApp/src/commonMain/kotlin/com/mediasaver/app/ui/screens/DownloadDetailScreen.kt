package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.formatDayHeader
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.ui.components.StickyBannerBar

/**
 * Full detail view for a single [DownloadRecord] — large thumbnail, all known metadata, and
 * Open / Share / Delete / Rename actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDetailScreen(
    record: DownloadRecord,
    confirmBeforeDelete: Boolean,
    onBack: () -> Unit,
    onDelete: (DownloadRecord) -> Unit,
    onRename: (newTitle: String, onResult: (Boolean) -> Unit) -> Unit
) {
    val info = record.mediaInfo
    val source = record.selectedSource
    val failed = record.outcome == DownloadOutcome.FAILED

    var pendingDelete by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var renameError by remember { mutableStateOf(false) }

    if (pendingDelete) {
        AlertDialog(
            onDismissRequest = { pendingDelete = false },
            title   = { Text("Delete this download?") },
            text    = { Text("This will remove the downloaded file from your device.") },
            confirmButton = {
                TextButton(onClick = { pendingDelete = false; onDelete(record); onBack() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = false }) { Text("Cancel") } }
        )
    }

    if (renaming) {
        var newTitle by remember { mutableStateOf(info.title) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Rename") },
            text = {
                Column {
                    OutlinedTextField(
                        value         = newTitle,
                        onValueChange = { newTitle = it; renameError = false },
                        singleLine    = true,
                        isError       = renameError,
                        supportingText = if (renameError) { { Text("Rename failed — try a different name.") } } else null,
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(newTitle) { success -> if (success) renaming = false else renameError = true }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
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
        bottomBar = { StickyBannerBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (info.thumbnailUrl.isNotBlank()) {
                    AsyncImage(model = info.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!failed && info.type != MediaType.IMAGE) {
                    Box(
                        modifier         = Modifier.size(56.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(info.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    info.uploader?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (failed) {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
                        Text(
                            record.errorMessage ?: "Download failed",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!failed) {
                            OutlinedButton(onClick = { openFolder(record.filePath) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp)); Text("Open")
                            }
                            OutlinedButton(onClick = { shareFile(record.filePath, source.mimeType) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp)); Text("Share")
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { renaming = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp)); Text("Rename")
                        }
                        OutlinedButton(
                            onClick  = { if (confirmBeforeDelete) pendingDelete = true else { onDelete(record); onBack() } },
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp)); Text("Delete")
                        }
                    }
                }

                HorizontalDivider()

                DetailRow("Platform", info.platform.displayName)
                info.durationSeconds?.let { DetailRow("Duration", "%d:%02d".format(it / 60, it % 60)) }
                source.resolution?.let { DetailRow("Resolution", it) }
                source.fps?.let { DetailRow("FPS", it.toString()) }
                source.fileSizeFormatted?.let { DetailRow("File size", it) }
                DetailRow("Format", source.ext.uppercase())
                DetailRow("Downloaded", formatDayHeader(record.timestampMs))
                if (!failed) DetailRow("Location", "Downloads")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
