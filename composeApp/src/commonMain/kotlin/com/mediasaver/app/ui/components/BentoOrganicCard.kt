package com.mediasaver.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.BentoDarkCardBg
import com.mediasaver.app.ui.theme.BentoWaveBlue
import com.mediasaver.app.ui.theme.BentoWavePurple

/**
 * High-contrast dark Bento card with fluid organic waves at the bottom and a clean white pill CTA button,
 * exactly matching the "Add New Devices" card in the reference mockup.
 */
@Composable
fun BentoOrganicCard(
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    height: Dp = 190.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(BentoDarkCardBg)
    ) {
        // ── Organic fluid waves background ────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Soft periwinkle wave on the left
            val bluePath = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(
                    w * 0.12f, h * 0.40f,
                    w * 0.22f, h * 0.70f,
                    w * 0.28f, h
                )
                lineTo(0f, h)
                close()
            }
            drawPath(bluePath, BentoWaveBlue)

            // 2. Vibrant purple wave on the right
            val purplePath = Path().apply {
                moveTo(w * 0.65f, h)
                cubicTo(
                    w * 0.68f, h * 0.55f,
                    w * 0.85f, h * 0.48f,
                    w, h * 0.58f
                )
                lineTo(w, h)
                close()
            }
            drawPath(purplePath, BentoWavePurple)
        }

        // ── Content: Title + Subtitle + Centered White Pill Button ────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Crisp white pill button
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BentoDarkCardBg
                ),
                contentPadding = PaddingValues(horizontal = 34.dp, vertical = 10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
