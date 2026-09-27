package com.mediasaver.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.BentoPurplePrimary

/**
 * Reusable pill switch matching the reference UI mockup ("ON" / "OFF" pill switch).
 */
@Composable
fun BentoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = BentoPurplePrimary,
    inactiveColor: Color = Color(0xFFE2E4EC),
    thumbColor: Color = Color.White
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "switchBg"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 30.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .width(58.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Label on the opposite side of the knob
        if (checked) {
            Text(
                text = "ON",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 9.dp)
            )
        } else {
            Text(
                text = "OFF",
                color = Color(0xFF75788D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }

        // Animated thumb knob with soft shadow
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
