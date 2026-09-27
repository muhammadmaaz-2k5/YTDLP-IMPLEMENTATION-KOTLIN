package com.mediasaver.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.*

private data class Feature(val icon: ImageVector, val label: String, val bg: Color, val textColor: Color)

private val FEATURES = listOf(
    Feature(Icons.Default.MoneyOff, "Free", BentoSageContainer, BentoSageText),
    Feature(Icons.Default.Bolt, "Fast HD", BentoSkyContainer, BentoSkyText),
    Feature(Icons.Default.Lock, "On-device", BentoLavenderContainer, BentoLavenderText)
)

/** Idle hero state with Bento styling. */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label         = "emptyFade"
    )

    BentoSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        cornerRadius = 28.dp,
        elevation = 2.dp,
        backgroundColor = BentoCardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BentoPurplePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Link,
                    contentDescription = null,
                    modifier           = Modifier.size(32.dp),
                    tint               = Color.White
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text       = "Ready to Download",
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = BentoTextPrimaryLight,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "Paste a link from YouTube, Instagram, Facebook, or TikTok to begin instant on-device extraction.",
                fontSize  = 13.sp,
                color     = BentoTextSecondaryLight,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FEATURES.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(feature.bg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = feature.textColor
                        )
                        Text(
                            text = feature.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = feature.textColor
                        )
                    }
                }
            }
        }
    }
}
