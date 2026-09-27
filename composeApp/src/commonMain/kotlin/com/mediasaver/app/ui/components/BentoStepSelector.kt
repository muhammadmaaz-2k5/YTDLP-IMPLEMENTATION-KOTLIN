package com.mediasaver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.BentoTextPrimaryLight

/**
 * Step selector control matching the `[-] 22.05°C [+]` controller on the AC device card in the reference mockup.
 */
@Composable
fun BentoStepSelector(
    valueText: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    unitText: String? = null,
    textColor: Color = BentoTextPrimaryLight,
    buttonBgColor: Color = Color.White
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Minus / Previous circular button
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(buttonBgColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPrevious
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Previous option",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Central value
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = valueText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            if (unitText != null) {
                Text(
                    text = unitText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }

        // Plus / Next circular button
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(buttonBgColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNext
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Next option",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
