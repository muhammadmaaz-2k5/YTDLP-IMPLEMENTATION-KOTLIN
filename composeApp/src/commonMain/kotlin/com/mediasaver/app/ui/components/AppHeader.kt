package com.mediasaver.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.*

data class HeaderAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * Modern Bento Header matching the reference UI mockup:
 * - Greeting mode (Home): 2-line minimalist hamburger menu on left + functional Premium badge on right; "Hello," + App Name.
 * - Navigation mode (Subpages): Back arrow or hamburger on left + centered/aligned title.
 */
@Composable
fun AppHeader(
    appName: String = "MediaSaver",
    tagline: String = "",
    logoIcon: ImageVector = Icons.Default.WorkspacePremium,
    actions: List<HeaderAction> = emptyList(),
    modifier: Modifier = Modifier,
    isGreetingMode: Boolean = true,
    greetingPrefix: String = "Hello,",
    isPremiumActive: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onPremiumClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {

    val isDark = isAppInDarkTheme()
    val primaryTextColor = if (isDark) BentoTextPrimaryDark else BentoTextPrimaryLight
    val secondaryTextColor = if (isDark) BentoTextSecondaryDark else BentoTextSecondaryLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        if (isGreetingMode) {
            // ── Top Navigation Row: Menu icon on Left + Avatar on Right ───────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minimalist 2-line hamburger menu icon
                val menuAction = onMenuClick ?: actions.firstOrNull()?.onClick
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { menuAction?.invoke() }
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryTextColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(15.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryTextColor)
                        )
                    }
                }

                // Functional Premium Status Pill / Button on Right
                val premiumAction = onPremiumClick ?: onAvatarClick ?: actions.firstOrNull {
                    it.contentDescription.contains("Premium", ignoreCase = true)
                }?.onClick

                if (premiumAction != null) {
                    val isPro = isPremiumActive
                    val chipBg = if (isPro) {
                        if (isDark) Color(0xFF2E2415) else Color(0xFFFEF3C7)
                    } else {
                        if (isDark) Color(0xFF251F1A) else BentoPeachContainer
                    }
                    val chipBorder = if (isPro) {
                        Color(0xFFF59E0B)
                    } else {
                        Color(0xFFF59E0B).copy(alpha = 0.45f)
                    }
                    val accentColor = if (isPro) Color(0xFFD97706) else Color(0xFFB45309)

                    Surface(
                        modifier = Modifier
                            .shadow(if (isPro) 4.dp else 2.dp, RoundedCornerShape(100.dp))
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { premiumAction.invoke() }
                            ),
                        shape = RoundedCornerShape(100.dp),
                        color = chipBg,
                        border = BorderStroke(1.2.dp, chipBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = if (isPro) "PRO Active" else "Unlock Premium",
                                tint = accentColor,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = if (isPro) "PRO ACTIVE" else "GET PRO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Greeting Text: "Hello," + App Name / User Name ────────────────
            Text(
                text = greetingPrefix,
                color = secondaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = appName,
                    color = primaryTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                // Optional subtle trailing action chips if provided
                if (actions.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        actions.forEach { action ->
                            BentoCircleBadge(
                                icon = action.icon,
                                contentDescription = action.contentDescription,
                                size = 38.dp,
                                iconSize = 18.dp,
                                tint = BentoPurplePrimary,
                                backgroundColor = if (isDark) BentoCardDark else BentoCardWhite,
                                onClick = action.onClick
                            )
                        }
                    }
                }
            }
        } else {
            // ── Detail / Subpage Mode ─────────────────────────────────────────
            val showSubpagePremium = onPremiumClick != null && appName != "Premium"

            if (actions.isNotEmpty()) {
                // Multi-control subpage (e.g. Downloads):
                // Row 1: Left [Back / Menu]  <--- spacer --->  Right [GET PRO / PRO]
                // Row 2: Left [Title + Subtitle]  <--- spacer --->  Right [Actions: Search, Clear]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Navigation bar row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (onBackClick != null) {
                            BentoCircleBadge(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                size = 42.dp,
                                iconSize = 20.dp,
                                backgroundColor = if (isDark) BentoCardDark else BentoCardWhite,
                                onClick = onBackClick
                            )
                        } else if (onMenuClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) BentoCardDark else BentoCardWhite)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onMenuClick
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(2.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(primaryTextColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .height(2.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(primaryTextColor)
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        if (showSubpagePremium) {
                            val isPro = isPremiumActive
                            val chipBg = if (isPro) {
                                if (isDark) Color(0xFF2E2415) else Color(0xFFFEF3C7)
                            } else {
                                if (isDark) Color(0xFF251F1A) else BentoPeachContainer
                            }
                            val chipBorder = if (isPro) Color(0xFFF59E0B) else Color(0xFFF59E0B).copy(alpha = 0.45f)
                            val accentColor = if (isPro) Color(0xFFD97706) else Color(0xFFB45309)

                            Surface(
                                modifier = Modifier
                                    .shadow(if (isPro) 3.dp else 1.dp, RoundedCornerShape(100.dp))
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onPremiumClick
                                    ),
                                shape = RoundedCornerShape(100.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, chipBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = if (isPro) "PRO Active" else "Unlock Premium",
                                        tint = accentColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = if (isPro) "PRO" else "GET PRO",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Title & Actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (tagline.isNotBlank()) {
                                Text(
                                    text = tagline,
                                    fontSize = 12.5.sp,
                                    color = secondaryTextColor,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actions.forEach { action ->
                                BentoCircleBadge(
                                    icon = action.icon,
                                    contentDescription = action.contentDescription,
                                    size = 40.dp,
                                    iconSize = 20.dp,
                                    tint = BentoPurplePrimary,
                                    backgroundColor = if (isDark) BentoCardDark else BentoCardWhite,
                                    onClick = action.onClick
                                )
                            }
                        }
                    }
                }
            } else {
                // Single-row subpage mode (e.g. Settings, Details, Privacy, Terms)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        BentoCircleBadge(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            size = 42.dp,
                            iconSize = 20.dp,
                            backgroundColor = if (isDark) BentoCardDark else BentoCardWhite,
                            onClick = onBackClick
                        )
                        Spacer(Modifier.width(16.dp))
                    } else if (onMenuClick != null) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDark) BentoCardDark else BentoCardWhite)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onMenuClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(primaryTextColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(primaryTextColor)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (tagline.isNotBlank()) {
                            Text(
                                text = tagline,
                                fontSize = 12.sp,
                                color = secondaryTextColor,
                                maxLines = 1
                            )
                        }
                    }

                    if (showSubpagePremium) {
                        val isPro = isPremiumActive
                        val chipBg = if (isPro) {
                            if (isDark) Color(0xFF2E2415) else Color(0xFFFEF3C7)
                        } else {
                            if (isDark) Color(0xFF251F1A) else BentoPeachContainer
                        }
                        val chipBorder = if (isPro) Color(0xFFF59E0B) else Color(0xFFF59E0B).copy(alpha = 0.45f)
                        val accentColor = if (isPro) Color(0xFFD97706) else Color(0xFFB45309)

                        Surface(
                            modifier = Modifier
                                .shadow(if (isPro) 3.dp else 1.dp, RoundedCornerShape(100.dp))
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onPremiumClick
                                ),
                            shape = RoundedCornerShape(100.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, chipBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = if (isPro) "PRO Active" else "Unlock Premium",
                                    tint = accentColor,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = if (isPro) "PRO" else "GET PRO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
