package com.mediasaver.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mediasaver.app.domain.model.PremiumPlan
import com.mediasaver.app.ui.components.AppBottomBarWithAd
import com.mediasaver.app.ui.theme.HeroGradient
import com.mediasaver.app.ui.theme.PremiumLavender

private data class PremiumFeature(val icon: androidx.compose.ui.graphics.vector.ImageVector, val text: String)

private val FEATURES = listOf(
    PremiumFeature(Icons.Default.WaterDrop, "Remove watermarks from downloads"),
    PremiumFeature(Icons.Default.MusicNote, "Extract audio-only in high quality"),
    PremiumFeature(Icons.Default.HighQuality, "Unlock the highest resolution formats")
)

/**
 * Premium plan picker — Monthly / 3 Months / Yearly.
 *
 * There's no billing SDK wired up (see [com.mediasaver.app.domain.repository.PremiumStore]):
 * this screen is honest about that rather than simulating a fake checkout, since the app has
 * no server and isn't distributed through Play Store.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    activePlan: PremiumPlan?,
    temporaryUnlockExpiresAt: Long?,
    onBack: () -> Unit,
    onSelectPlan: (PremiumPlan) -> Unit,
    onWatchRewardedAd: (onResult: (earned: Boolean) -> Unit) -> Unit,
    onWatchRewardedInterstitialAd: (onResult: (earned: Boolean) -> Unit) -> Unit,
    onOpenHome: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var selected by remember(activePlan) { mutableStateOf(activePlan ?: PremiumPlan.YEARLY) }
    var confirmed by remember { mutableStateOf(false) }
    var rewardedAdUnavailable by remember { mutableStateOf(false) }
    val temporarilyUnlocked = temporaryUnlockExpiresAt != null

    // See HomeScreen for why this is a Box + overlaid nav bar rather than Scaffold(bottomBar = …).
    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Surface(onClick = onBack, modifier = Modifier.padding(8.dp).size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Full gradient hero banner (replaces a plain icon+text column) — same visual weight
            // as the glass AppHeader on other screens, but warmer/brand-forward since this is the
            // upsell screen.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(HeroGradient)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 220.dp, y = (-40).dp)
                        .background(
                            brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)),
                            shape = CircleShape
                        )
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp)
                ) {
                    Box(
                        modifier         = Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Unlock Premium", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Get the most out of every download",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FEATURES.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier         = Modifier.size(32.dp).clip(CircleShape).background(PremiumLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Text(feature.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Real reward, not a fake purchase: watching to completion grants an honest 24-hour
            // unlock — the actual trade being offered, since there's no billing backend.
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            if (temporarilyUnlocked) "Free Premium active" else "Watch an ad for free Premium",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        if (temporarilyUnlocked) "Watch another ad anytime to add more free time."
                        else "Watch an ad to unlock every Premium feature for free — no account, no payment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                rewardedAdUnavailable = false
                                onWatchRewardedAd { earned -> if (!earned) rewardedAdUnavailable = true }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Watch ad (+24h)") }
                        OutlinedButton(
                            onClick = {
                                rewardedAdUnavailable = false
                                onWatchRewardedInterstitialAd { earned -> if (!earned) rewardedAdUnavailable = true }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Longer ad (+48h)") }
                    }
                    if (rewardedAdUnavailable) {
                        Text(
                            "No ad is ready right now — try again in a moment.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumPlan.entries.forEach { plan ->
                    PlanCard(
                        plan       = plan,
                        isSelected = plan == selected,
                        isActive   = plan == activePlan,
                        onClick    = { selected = plan }
                    )
                }
            }

            if (confirmed && activePlan != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("${activePlan.label} plan active", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Button(
                onClick  = { onSelectPlan(selected); confirmed = true },
                enabled  = activePlan != selected,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    if (activePlan == selected) "Current plan" else "Continue with ${selected.label}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text      = "Demo mode — this unlocks the premium UI locally. No billing " +
                    "provider is connected and no payment is charged.",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(170.dp))
        }
    }

        AppBottomBarWithAd(
            selectedIdx = 2,
            onHome      = onOpenHome,
            onDownloads = onOpenDownloads,
            onPremium   = { /* already premium */ },
            onSettings  = onOpenSettings,
            modifier    = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border   = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    plan.badge?.let { badge ->
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = Color.Transparent) {
                            Box(modifier = Modifier.clip(MaterialTheme.shapes.extraSmall).background(HeroGradient)) {
                                Text(badge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                        }
                    }
                    if (isActive) {
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text("Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                plan.perMonthEquivalent?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(plan.price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(plan.period, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
