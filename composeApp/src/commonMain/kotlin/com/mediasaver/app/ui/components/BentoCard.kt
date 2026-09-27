package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mediasaver.app.ui.theme.*

/**
 * Reusable Bento Surface Card — elevated, softly-rounded container matching the reference UI design.
 */
@Composable
fun BentoSurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isAppInDarkTheme()
    val bg = backgroundColor ?: if (isDark) BentoCardDark else BentoCardWhite
    val border = borderColor ?: if (isDark) BentoBorderDark else BentoBorderLight
    val shape = RoundedCornerShape(cornerRadius)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.04f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.06f)
            )
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .then(clickableModifier)
    ) {
        content()
    }
}
