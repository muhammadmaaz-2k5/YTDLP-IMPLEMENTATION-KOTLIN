package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mediasaver.app.ui.theme.GradientEnd
import com.mediasaver.app.ui.theme.HeroGradient
import com.mediasaver.app.ui.theme.OnboardingPurpleTop
import com.mediasaver.app.ui.theme.PremiumLavender

/** One-time first-launch screen. Calls [onFinished] when the user taps the arrow CTA. */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ── Purple hero backdrop (top ~58%) ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .background(Brush.verticalGradient(listOf(OnboardingPurpleTop, HeroGradientEndSoft)))
        )

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            // ── Stacked preview cards + logo mark ────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                StackedCard(
                    width   = 150.dp,
                    height  = 190.dp,
                    rotationDeg = 10f,
                    offsetX = 22.dp,
                    color   = Color.White.copy(alpha = 0.25f)
                )
                StackedCard(
                    width   = 150.dp,
                    height  = 190.dp,
                    rotationDeg = -8f,
                    offsetX = (-18).dp,
                    color   = Color.White.copy(alpha = 0.35f)
                )
                StackedCard(
                    width   = 150.dp,
                    height  = 190.dp,
                    rotationDeg = 0f,
                    offsetX = 0.dp,
                    color   = Color(0xFF3C63C9)
                )
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                shape = CircleShape,
                color = Color.Black,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector        = Icons.Default.FastRewind,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── White bottom sheet ────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = PremiumLavender) {
                        Text(
                            text     = "MediaSaver",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text       = "All Videos Download",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign  = TextAlign.Center,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "One-click Fast Download",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    IconButton(
                        onClick  = onFinished,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(HeroGradient)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Get started", tint = Color.White)
                    }
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

private val HeroGradientEndSoft = GradientEnd

@Composable
private fun StackedCard(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, rotationDeg: Float, offsetX: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .rotate(rotationDeg)
            .size(width, height)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
    )
}
