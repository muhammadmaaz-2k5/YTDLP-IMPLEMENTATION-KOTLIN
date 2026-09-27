package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mediasaver.app.data.platform.formatDayHeader
import com.mediasaver.app.data.platform.openFolder
import com.mediasaver.app.data.platform.shareFile
import com.mediasaver.app.domain.model.DownloadOutcome
import com.mediasaver.app.domain.model.DownloadRecord
import com.mediasaver.app.domain.model.MediaType
import com.mediasaver.app.ui.components.AppHeader
import com.mediasaver.app.ui.components.BentoCircleBadge
import com.mediasaver.app.ui.components.BentoSurfaceCard
import com.mediasaver.app.ui.components.StickyBannerBar
import com.mediasaver.app.ui.theme.*

/**
 * Modern Bento Detail view for a single [DownloadRecord].
 */
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
    val clipboard = LocalClipboardManager.current

    if (pendingDelete) {
        AlertDialog(
            onDismissRequest = { pendingDelete = false },
            title   = { Text("Delete this download?", fontWeight = FontWeight.Bold) },
            text    = { Text("This will remove the downloaded file from your device.") },
            confirmButton = {
                TextButton(onClick = { pendingDelete = false; onDelete(record); onBack() }) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = false }) { Text("Cancel") } }
        )
    }

    if (renaming) {
        var newTitle by remember { mutableStateOf(info.title) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Rename", fontWeight = FontWeight.Bold) },
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
                }) { Text("Save", color = BentoPurplePrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } }
        )
    }

    val isDark = isAppInDarkTheme()
    val canvasBg = if (isDark) BentoBackgroundDark else BentoBackgroundLight

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(canvasBg).statusBarsPadding().padding(horizontal = 18.dp, vertical = 8.dp)) {
                AppHeader(
                    appName = "Details",
                    tagline = info.platform.displayName,
                    logoIcon = Icons.Default.Image,
                    isGreetingMode = false,
                    onBackClick = onBack
                )
            }
        },
        containerColor = canvasBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── Large Bento Thumbnail ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .shadow(4.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(BentoSkyContainer),
                contentAlignment = Alignment.Center
            ) {
                if (info.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = info.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = BentoSkySubtext
                    )
                }
                if (!failed && info.type != MediaType.IMAGE) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // ── Title & Creator Card ──────────────────────────────────────────
            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = info.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimaryLight
                    )
                    info.uploader?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = BentoTextSecondaryLight
                        )
                    }
                }
            }

            // ── Action Buttons Row ────────────────────────────────────────────
            if (!failed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { openFolder(record.filePath) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPurplePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { shareFile(record.filePath, source.mimeType) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BentoTextPrimaryLight
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { renaming = true },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rename")
                    }

                    OutlinedButton(
                        onClick = {
                            if (confirmBeforeDelete) pendingDelete = true else { onDelete(record); onBack() }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            } else {
                // ── Failed State Bento Card ───────────────────────────────────────
                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    backgroundColor = BentoPeachContainer,
                    borderColor = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BentoCircleBadge(
                                icon = Icons.Default.ErrorOutline,
                                tint = ErrorRed,
                                backgroundColor = Color.White,
                                size = 36.dp
                            )
                            Text(
                                "Download Incomplete",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                        Text(
                            text = record.errorMessage ?: "The media stream could not be completely downloaded from the source platform.",
                            fontSize = 13.sp,
                            color = BentoTextPrimaryLight,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = { onDelete(record); onBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Remove from History", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Storage Location Bento Card ───────────────────────────────────
            if (!failed && record.filePath.isNotBlank()) {
                val clipboard = LocalClipboardManager.current
                var copied by remember { mutableStateOf(false) }

                BentoSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Storage Location",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimaryLight
                            )
                            Surface(
                                onClick = {
                                    clipboard.setText(AnnotatedString(record.filePath))
                                    copied = true
                                },
                                shape = RoundedCornerShape(100.dp),
                                color = if (copied) BentoSageContainer else BentoLavenderContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy path",
                                        tint = if (copied) BentoSageText else BentoPurplePrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (copied) "Copied!" else "Copy Path",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (copied) BentoSageText else BentoPurplePrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = record.filePath,
                            fontSize = 12.sp,
                            color = BentoTextSecondaryLight,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ── Viral Content Tools: Hooks, Tags & Keywords ──────────────────
            val effectiveTags = remember(info) {
                if (info.tags.isNotEmpty()) info.tags
                else info.title.split(" ")
                    .map { it.replace(Regex("[^A-Za-z0-9]"), "") }
                    .filter { it.length in 3..20 }
                    .distinct()
                    .take(12)
            }
            var hookCopied by remember { mutableStateOf(false) }
            var tagsCopied by remember { mutableStateOf(false) }
            var captionCopied by remember { mutableStateOf(false) }

            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Content Tools (Hooks & Tags)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimaryLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                clipboard.setText(AnnotatedString(info.title))
                                hookCopied = true
                            },
                            shape = RoundedCornerShape(100.dp),
                            color = if (hookCopied) BentoSageContainer else BentoLavenderContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (hookCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = if (hookCopied) BentoSageText else BentoPurplePrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (hookCopied) "Hook Copied ✓" else "Copy Hook",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hookCopied) BentoSageText else BentoPurplePrimary
                                )
                            }
                        }

                        if (effectiveTags.isNotEmpty()) {
                            Surface(
                                onClick = {
                                    val formatted = effectiveTags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
                                    clipboard.setText(AnnotatedString(formatted))
                                    tagsCopied = true
                                },
                                shape = RoundedCornerShape(100.dp),
                                color = if (tagsCopied) BentoSageContainer else BentoLavenderContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (tagsCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = if (tagsCopied) BentoSageText else BentoPurplePrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (tagsCopied) "Tags Copied ✓" else "Copy Tags",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tagsCopied) BentoSageText else BentoPurplePrimary
                                    )
                                }
                            }
                        }

                        if (!info.description.isNullOrBlank()) {
                            Surface(
                                onClick = {
                                    clipboard.setText(AnnotatedString(info.description))
                                    captionCopied = true
                                },
                                shape = RoundedCornerShape(100.dp),
                                color = if (captionCopied) BentoSageContainer else BentoLavenderContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (captionCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = if (captionCopied) BentoSageText else BentoPurplePrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (captionCopied) "Caption Copied ✓" else "Copy Caption",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (captionCopied) BentoSageText else BentoPurplePrimary
                                    )
                                }
                            }
                        }
                    }

                    if (effectiveTags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            effectiveTags.take(10).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        text = if (tag.startsWith("#")) tag else "#$tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BentoTextSecondaryLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Metadata Bento Card ───────────────────────────────────────────
            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailMetaRow("Platform", info.platform.displayName)
                    HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                    DetailMetaRow("Format", "${source.ext.uppercase()} · ${source.label}")
                    source.resolution?.let {
                        HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                        DetailMetaRow("Resolution", it)
                    }
                    source.fileSizeFormatted?.let {
                        HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                        DetailMetaRow("File size", it)
                    }
                    info.durationSeconds?.let { dur ->
                        HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                        DetailMetaRow("Duration", "%d:%02d".format(dur / 60, dur % 60))
                    }
                    HorizontalDivider(color = BentoBorderLight.copy(alpha = 0.6f))
                    DetailMetaRow("Downloaded", formatDayHeader(record.timestampMs))
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DetailMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = BentoTextSecondaryLight)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
