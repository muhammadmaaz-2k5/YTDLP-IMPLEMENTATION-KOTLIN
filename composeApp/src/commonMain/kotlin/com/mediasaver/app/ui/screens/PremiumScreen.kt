package com.mediasaver.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.data.platform.currentTimeMs
import com.mediasaver.app.domain.model.PremiumPlan
import com.mediasaver.app.ui.components.*
import com.mediasaver.app.ui.theme.*

private data class PremiumFeature(val icon: ImageVector, val text: String, val bg: Color, val textColor: Color)

private val FEATURES = listOf(
    PremiumFeature(Icons.Default.WaterDrop, "Remove watermarks from downloads", BentoSageContainer, BentoSageText),
    PremiumFeature(Icons.Default.MusicNote, "Extract audio-only in high quality", BentoSkyContainer, BentoSkyText),
    PremiumFeature(Icons.Default.HighQuality, "Unlock highest resolution formats", BentoLavenderContainer, BentoLavenderText)
)

@Composable
fun PremiumScreen(
    activePlan: PremiumPlan?,
    temporaryUnlockExpiresAt: Long?,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onSelectPlan: (PremiumPlan) -> Unit,
    onWatchRewardedAd: (onResult: (earned: Boolean) -> Unit) -> Unit,
    onWatchRewardedInterstitialAd: (onResult: (earned: Boolean) -> Unit) -> Unit,
    onOpenHome: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var selected by remember(activePlan) { mutableStateOf(activePlan ?: PremiumPlan.YEARLY) }
    var confirmed by remember { mutableStateOf(false) }
    var rewardedAdUnavailable by remember { mutableStateOf(false) }
    val temporarilyUnlocked = temporaryUnlockExpiresAt != null

    val isDark = isAppInDarkTheme()
    val canvasBg = if (isDark) BentoBackgroundDark else BentoBackgroundLight
    val textPrimary = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val textSecondary = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight
    val cardBg = if (isDark) BentoCardDark else BentoCardWhite
    val borderCol = if (isDark) BentoBorderDark else BentoBorderLight

    Box(modifier = Modifier.fillMaxSize().background(canvasBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppHeader(
                appName = "Premium",
                tagline = "Unlock full capabilities",
                logoIcon = Icons.Default.WorkspacePremium,
                isGreetingMode = false,
                onBackClick = onBack,
                onMenuClick = onOpenDrawer
            )

            // ── Hero Banner: Bento Organic Waves Card ─────────────────────────
            BentoOrganicCard(
                title = "Unlock Full Power",
                subtitle = "Highest quality · No watermarks · Fast processing",
                buttonText = "Choose Plan",
                onButtonClick = { confirmed = true },
                height = 190.dp
            )

            // ── Feature Badges Bento Group ────────────────────────────────────
            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FEATURES.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BentoCircleBadge(
                                icon = feature.icon,
                                size = 36.dp,
                                iconSize = 18.dp,
                                tint = feature.textColor,
                                backgroundColor = feature.bg,
                                elevation = 1.dp
                            )
                            Text(
                                text = feature.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextPrimaryLight
                            )
                        }
                    }
                }
            }

            // ── Ad-Supported Free Unlock Card ─────────────────────────────────
            BentoSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                backgroundColor = BentoSkyContainer,
                borderColor = Color.Transparent,
                elevation = 2.dp
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
                            icon = Icons.Default.PlayCircle,
                            size = 36.dp,
                            iconSize = 20.dp,
                            tint = BentoPurplePrimary,
                            backgroundColor = Color.White
                        )
                        val remainingDesc = remember(temporaryUnlockExpiresAt) {
                            temporaryUnlockExpiresAt?.let {
                                val diffMs = it - currentTimeMs()
                                if (diffMs > 0) {
                                    val hours = diffMs / 3_600_000L
                                    val mins = (diffMs % 3_600_000L) / 60_000L
                                    if (hours > 0) "${hours}h ${mins}m left" else "${mins}m left"
                                } else null
                            }
                        }
                        Column {
                            Text(
                                text = if (temporarilyUnlocked) "Free Premium Active" else "Watch an Ad for Free Premium",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSkyText
                            )
                            if (remainingDesc != null) {
                                Text(
                                    text = "Expires in $remainingDesc",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPurplePrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (temporarilyUnlocked) "Watch another ad anytime to stack more free time."
                               else "Watch an ad to unlock every feature for free — no payment required.",
                        fontSize = 12.sp,
                        color = BentoSkySubtext
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                rewardedAdUnavailable = false
                                onWatchRewardedAd { earned ->
                                    if (!earned) rewardedAdUnavailable = true
                                }
                            },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPurplePrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Watch Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("+24h Free", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, color = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        Button(
                            onClick = {
                                rewardedAdUnavailable = false
                                onWatchRewardedInterstitialAd { earned ->
                                    if (!earned) rewardedAdUnavailable = true
                                }
                            },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF232E4A) else Color.White,
                                contentColor = if (isDark) Color.White else BentoSkyText
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Longer Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("+48h Free", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, color = BentoPurplePrimary)
                            }
                        }
                    }

                    if (rewardedAdUnavailable) {
                        Text(
                            text = "No ad ready yet — please check connection.",
                            fontSize = 11.sp,
                            color = ErrorRed
                        )
                    }
                }
            }

            // ── Plan Selection Cards ──────────────────────────────────────────
            Text(
                text = "Membership Plans",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumPlan.entries.forEach { plan ->
                    val isSelected = plan == selected
                    BentoSurfaceCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = plan },
                        cornerRadius = 24.dp,
                        backgroundColor = if (isSelected) {
                            if (isDark) BentoLavenderContainerDark else BentoLavenderContainer
                        } else cardBg,
                        borderColor = if (isSelected) BentoPurplePrimary else borderCol,
                        elevation = if (isSelected) 3.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular check indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BentoPurplePrimary else Color.Transparent)
                                    .border(2.dp, if (isSelected) BentoPurplePrimary else borderCol, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Plan label & perMonth details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFFDDD6FE) else BentoPurplePrimary
                                    } else textPrimary
                                )
                                plan.perMonthEquivalent?.let { perMonth ->
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "$perMonth · cancel anytime",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            // Right Column: Badge & Price cleanly aligned
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                plan.badge?.let { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = BentoPurplePrimary
                                    ) {
                                        Text(
                                            text = badge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = plan.price,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) {
                                            if (isDark) Color(0xFFDDD6FE) else BentoPurplePrimary
                                        } else textPrimary
                                    )
                                    Text(
                                        text = " ${plan.period}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textSecondary,
                                        modifier = Modifier.padding(bottom = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Confirm selection pill button
            Button(
                onClick = { onSelectPlan(selected); confirmed = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPurplePrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Select ${selected.label} Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (confirmed) {
                Text(
                    text = "Plan selected locally (sideload build — no Play Billing required)",
                    fontSize = 12.sp,
                    color = BentoTextSecondaryLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
