package com.mediasaver.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.*

/**
 * Modern Bento Quick-Start card explaining the 3-step download process in an intuitive,
 * manageable way so any user immediately understands how to use the app.
 */
@Composable
fun HowItWorksCard(
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    BentoSurfaceCard(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        cornerRadius = 24.dp,
        backgroundColor = BentoCardWhite,
        borderColor = BentoBorderLight,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with expandable toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoCircleBadge(
                        icon = Icons.Default.HelpOutline,
                        tint = BentoPurplePrimary,
                        backgroundColor = BentoLavenderContainer,
                        size = 36.dp,
                        iconSize = 18.dp,
                        elevation = 0.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "How to Download",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "3 simple steps to save any media",
                            fontSize = 12.sp,
                            color = BentoTextSecondaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = BentoTextPrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Always visible 3-Step Pill Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepChip(number = "1", label = "Copy Link", icon = Icons.Default.ContentCopy, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFC7CBD9),
                    modifier = Modifier.size(14.dp).padding(horizontal = 2.dp)
                )
                StepChip(number = "2", label = "Paste Here", icon = Icons.Default.ContentPaste, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFC7CBD9),
                    modifier = Modifier.size(14.dp).padding(horizontal = 2.dp)
                )
                StepChip(number = "3", label = "Save HD", icon = Icons.Default.Download, modifier = Modifier.weight(1f))
            }

            // Expanded Details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(
                        num = "1",
                        title = "Copy the link",
                        desc = "Open YouTube, Instagram, Facebook, or TikTok. Tap Share on any video/reel and select 'Copy Link'."
                    )
                    DetailRow(
                        num = "2",
                        title = "Paste or Auto-Detect",
                        desc = "MediaSaver automatically detects copied links when you switch back. Or tap the Paste button above."
                    )
                    DetailRow(
                        num = "3",
                        title = "Choose format & quality",
                        desc = "Pick Full HD 1080p, 720p, or Audio Only (MP3/M4A), then tap Download. Done!"
                    )
                }
            }
        }
    }
}

@Composable
private fun StepChip(
    number: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF7F8FC)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BentoPurplePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailRow(
    num: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BentoPurplePrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = num,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimaryLight
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = BentoTextSecondaryLight,
                lineHeight = 17.sp
            )
        }
    }
}
