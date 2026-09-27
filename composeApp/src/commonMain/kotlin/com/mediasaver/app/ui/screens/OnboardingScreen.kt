package com.mediasaver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.components.BentoCircleBadge
import com.mediasaver.app.ui.components.BentoSurfaceCard
import com.mediasaver.app.ui.theme.BentoBackgroundLight
import com.mediasaver.app.ui.theme.BentoCardWhite
import com.mediasaver.app.ui.theme.BentoDarkCardBg
import com.mediasaver.app.ui.theme.BentoLavenderContainer
import com.mediasaver.app.ui.theme.BentoPurplePrimary
import com.mediasaver.app.ui.theme.BentoSageContainer
import com.mediasaver.app.ui.theme.BentoSkyContainer

/** One-time first-launch screen styled in the modern Bento pastel aesthetic. */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Top Brand Chip ───────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(50),
                color = BentoLavenderContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = BentoPurplePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Next-Gen Media Saver",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.weight(0.8f))

            // ── Bento Stacked Pastel Cards Preview ───────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(260.dp)
            ) {
                // Background Card 1 (Lavender - tilted right)
                BentoSurfaceCard(
                    modifier = Modifier
                        .offset(x = 28.dp, y = 10.dp)
                        .rotate(10f)
                        .size(190.dp, 210.dp),
                    backgroundColor = BentoLavenderContainer,
                    cornerRadius = 28.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        BentoCircleBadge(
                            icon = Icons.Default.PlayArrow,
                            backgroundColor = Color.White,
                            tint = BentoPurplePrimary,
                            size = 36.dp,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                // Background Card 2 (Sky Blue - tilted left)
                BentoSurfaceCard(
                    modifier = Modifier
                        .offset(x = (-24).dp, y = (-6).dp)
                        .rotate(-8f)
                        .size(190.dp, 210.dp),
                    backgroundColor = BentoSkyContainer,
                    cornerRadius = 28.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        BentoCircleBadge(
                            icon = Icons.Default.Download,
                            backgroundColor = Color.White,
                            tint = BentoPurplePrimary,
                            size = 36.dp,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }

                // Front Featured Card (Sage Green - center)
                BentoSurfaceCard(
                    modifier = Modifier
                        .size(200.dp, 220.dp),
                    backgroundColor = BentoSageContainer,
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        BentoCircleBadge(
                            icon = Icons.Default.Download,
                            backgroundColor = Color.White,
                            tint = Color(0xFF2E6930),
                            size = 48.dp
                        )

                        Column {
                            Text(
                                text = "Ultra Fast",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2022)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "4K, HD & MP3 ready",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5A665A)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Bottom Bento Action Card ─────────────────────────────────────
            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BentoCardWhite,
                cornerRadius = 32.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hello,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8F95B2),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Save Any Media",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = BentoDarkCardBg
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Effortless video and audio downloads with crystal-clear quality and on-device privacy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757A95),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    // Pill CTA Button
                    Button(
                        onClick = onFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPurplePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Get Started",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

