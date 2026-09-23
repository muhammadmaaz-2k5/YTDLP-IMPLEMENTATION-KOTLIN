package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord

/**
 * A single row in the Downloads list — thumbnail, title/subtitle, size (or progress for a live
 * download), and a 3-dot menu with Open / Share / Delete actions. Tapping the row itself opens
 * the detail screen.
 */
@Composable
fun DownloadRow(
    record: DownloadRecord,
    onOpenDetail: (DownloadRecord) -> Unit,
    onDeleteRequest: (DownloadRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val failed = record.outcome == DownloadOutcome.FAILED
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenDetail(record) }
            .padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (record.mediaInfo.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model              = record.mediaInfo.thumbnailUrl,
                    contentDescription = null,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector        = Icons.Default.Image,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = record.mediaInfo.title,
                style    = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color    = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text     = if (failed)
                    (record.errorMessage?.take(60) ?: "Download failed")
                else
                    "${record.mediaInfo.platform.displayName} · ${record.selectedSource.label}",
                style    = MaterialTheme.typography.labelSmall,
                color    = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (failed) 0.35f else 1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text  = record.selectedSource.fileSizeFormatted ?: if (failed) "Failed" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                if (!failed) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                        onClick = { menuExpanded = false; openFolder(record.filePath) }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = { menuExpanded = false; shareFile(record.filePath, record.selectedSource.mimeType) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { menuExpanded = false; onDeleteRequest(record) }
                )
            }
        }
    }
}
