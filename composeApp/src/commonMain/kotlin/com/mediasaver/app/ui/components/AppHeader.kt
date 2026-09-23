package com.mediasaver.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediasaver.app.ui.theme.Brand400
import com.mediasaver.app.ui.theme.Brand600
import com.mediasaver.app.ui.theme.GradientEnd
import com.mediasaver.app.ui.theme.GradientStart

// The glass card's background/border/text colors are read from MaterialTheme.colorScheme inside
// AppHeader() itself (not module-level constants) — same MaterialTheme.colorScheme.inverseSurface
// token FloatingPillNavBar uses, so the header actually responds to Settings' Appearance
// (Light/Dark/System) instead of being permanently dark regardless of the chosen theme.
private val LogoGradient     = Brush.linearGradient(
    colors  = listOf(GradientStart, GradientEnd),
    start   = Offset(0f, 0f),
    end     = Offset(60f, 60f)
)
private val ShimmerGradient = Brush.linearGradient(
    listOf(
        Color.White.copy(0.0f),
        Color.White.copy(0.18f),
        Color.White.copy(0.0f)
    )
)

/**
 * Reusable professional app header — a dark glassmorphic card with:
 *
 * - **Left**: gradient brand logo pill with an icon + animated shimmer sweep
 * - **Centre**: app name (bold) + tagline (dimmed)
 * - **Right**: one or two icon action buttons (e.g. info, premium)
 *
 * Designed to complement [FloatingPillNavBar] — same dark-surface language,
 * same corner-radius convention, same ambient shadow depth.
 *
 * ```kotlin
 * AppHeader(
 *     appName    = "MediaSaver",
 *     tagline    = "Download from any platform",
 *     logoIcon   = Icons.Outlined.Download,
 *     actions    = listOf(
 *         HeaderAction(Icons.Default.Info,             "About")    { showAbout = true },
 *         HeaderAction(Icons.Default.WorkspacePremium, "Premium")  { openPremium() }
 *     )
 * )
 * ```
 *
 * @param appName   Bold title text.
 * @param tagline   Smaller subtitle shown beneath the name.
 * @param logoIcon  Icon drawn inside the gradient logo pill on the left.
 * @param actions   Up to 3 trailing icon-button actions (right side).
 * @param modifier  Outer modifier — defaults fill the available width.
 */
@Composable
fun AppHeader(
    appName:   String,
    tagline:   String,
    logoIcon:  ImageVector,
    actions:   List<HeaderAction> = emptyList(),
    modifier:  Modifier = Modifier
) {
    // ── Shimmer loop on the logo pill ────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "logoShimmer")
    val shimmerX by inf.animateFloat(
        initialValue  = -120f,
        targetValue   = 280f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2800, easing = LinearEasing, delayMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    // Theme-driven, not fixed constants — see the comment above LogoGradient.
    val headerBg     = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f)
    val headerOnBg   = MaterialTheme.colorScheme.inverseOnSurface
    val headerBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Glassmorphic dark card
                .shadow(
                    elevation    = 20.dp,
                    shape        = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor    = Brand600.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(headerBg)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            headerOnBg.copy(alpha = 0.12f),
                            headerBorder,
                            headerOnBg.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Logo pill ─────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LogoGradient)
                    // Shimmer sweep overlay
                    .drawBehind {
                        drawRect(
                            brush  = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerX, 0f),
                                end   = Offset(shimmerX + 80f, size.height)
                            ),
                            size   = this.size
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = logoIcon,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(26.dp)
                )
            }

            // ── Title + tagline ───────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = appName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = headerOnBg,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text  = tagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = headerOnBg.copy(alpha = 0.65f),
                    letterSpacing = 0.sp
                )
            }

            // ── Trailing actions ──────────────────────────────────────────────
            actions.forEach { action ->
                HeaderIconButton(action)
            }
        }

        // ── Subtle purple glow at the top-left corner ─────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-10).dp, y = (-10).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Brand400.copy(alpha = 0.18f), Color.Transparent),
                        radius = 80f
                    ),
                    shape = CircleShape
                )
        )
    }
}

// ── Action button data holder ─────────────────────────────────────────────────

/**
 * Describes a single trailing icon action in [AppHeader].
 *
 * @param icon               Icon to display.
 * @param contentDescription Accessibility label.
 * @param badgeCount         When > 0, a red badge dot is drawn on the icon.
 * @param onClick            Invoked on tap.
 */
data class HeaderAction(
    val icon:               ImageVector,
    val contentDescription: String,
    val badgeCount:         Int  = 0,
    val onClick:            () -> Unit
)

// ── Individual icon button ────────────────────────────────────────────────────

@Composable
private fun HeaderIconButton(action: HeaderAction) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "iconScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (isPressed) 0.18f else 0.10f,
        animationSpec = tween(120),
        label         = "iconBgAlpha"
    )

    val onHeaderBg = MaterialTheme.colorScheme.inverseOnSurface

    Box(
        modifier         = Modifier
            .size(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(12.dp))
            .background(onHeaderBg.copy(alpha = bgAlpha))
            .clickable(interactionSource, indication = null, onClick = action.onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = action.icon,
            contentDescription = action.contentDescription,
            tint               = onHeaderBg.copy(alpha = 0.85f),
            modifier           = Modifier.size(20.dp)
        )

        // Badge dot
        if (action.badgeCount > 0) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5370))  // ErrorRed
            )
        }
    }
}
