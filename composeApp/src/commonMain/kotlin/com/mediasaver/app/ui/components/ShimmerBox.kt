package com.mediasaver.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Animated shimmer placeholder for loading states. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    radius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1000f,
        targetValue  = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    val colors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant
    )
    val brush = Brush.linearGradient(
        colors = colors,
        start  = Offset(shimmerX, 0f),
        end    = Offset(shimmerX + 600f, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(brush)
    )
}

/** Full loading skeleton matching the MediaPreviewCard layout. */
@Composable
fun MediaCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), radius = 16.dp)
        Spacer(Modifier.height(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp))
        Spacer(Modifier.height(8.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
        Spacer(Modifier.height(16.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(48.dp), radius = 12.dp)
    }
}
