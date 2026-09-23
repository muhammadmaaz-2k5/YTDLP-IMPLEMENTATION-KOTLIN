package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.MediaType

/** Home-screen "Recently Download" card — the single most recent completed download, with a big thumbnail. */
@Composable
fun RecentDownloadCard(record: DownloadRecord, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = record.mediaInfo.type.emoji,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = record.mediaInfo.title,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text     = record.mediaInfo.uploader ?: record.mediaInfo.platform.displayName,
                        style    = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (record.outcome == DownloadOutcome.COMPLETE) {
                    IconButton(onClick = { openFolder(record.filePath) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick  = { shareFile(record.filePath, record.selectedSource.mimeType) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
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
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
                if (record.mediaInfo.type != MediaType.IMAGE) {
                    Box(
                        modifier         = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}
