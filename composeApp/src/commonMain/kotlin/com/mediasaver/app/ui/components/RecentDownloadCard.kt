package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.ui.theme.BentoCardWhite
import com.mediasaver.app.ui.theme.BentoPurplePrimary
import com.mediasaver.app.ui.theme.BentoTextPrimaryLight
import com.mediasaver.app.ui.theme.BentoTextSecondaryLight

/**
 * Modern Bento "Recently Download" card — clean white card surface, rounded corners, large preview, and quick actions.
 */
@Composable
fun RecentDownloadCard(record: DownloadRecord, modifier: Modifier = Modifier) {
    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        elevation = 3.dp,
        backgroundColor = BentoCardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Emoji badge + Title + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular emoji/type badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = record.mediaInfo.type.emoji,
                        fontSize = 18.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.mediaInfo.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = BentoTextPrimaryLight
                    )
                    Text(
                        text = record.mediaInfo.uploader ?: record.mediaInfo.platform.displayName,
                        fontSize = 12.sp,
                        color = BentoTextSecondaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Circular action buttons
                if (record.outcome == DownloadOutcome.COMPLETE) {
                    BentoCircleBadge(
                        icon = Icons.Default.FolderOpen,
                        contentDescription = "Open Folder",
                        size = 36.dp,
                        iconSize = 18.dp,
                        tint = BentoPurplePrimary,
                        elevation = 2.dp,
                        onClick = { openFolder(record.filePath) }
                    )
                    BentoCircleBadge(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        size = 36.dp,
                        iconSize = 18.dp,
                        tint = BentoPurplePrimary,
                        elevation = 2.dp,
                        onClick = { shareFile(record.filePath, record.selectedSource.mimeType) }
                    )
                }
            }

            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEBEBF2)),
                contentAlignment = Alignment.Center
            ) {
                if (record.mediaInfo.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = record.mediaInfo.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (record.mediaInfo.type != MediaType.IMAGE) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
