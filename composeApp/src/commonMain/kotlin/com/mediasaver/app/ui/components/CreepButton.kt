package com.mediasaver.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Default colour tokens ────────────────────────────────────────────────────
private val ButtonRed    = Color(0xFFEF5340)   // coral-red pill (matches reference)
private val BaseDark     = Color(0xFF1A1A1A)   // near-black base / shadow layer
private val EyeWhite     = Color(0xFFEEEEEE)   // eye sclera
private val EyePupil     = Color(0xFF222222)   // pupil
private val LabelWhite   = Color(0xFFFFFFFF)   // button label

/**
 * A reusable "creep" button that slides upward on hover/press,
 * revealing two animated eyes peeking from the dark base underneath.
 *
 * Works on both **Desktop** (hover-triggered) and **Android** (press-triggered).
 *
 * ```kotlin
 * CreepButton(
 *     label    = "Download",
 *     onClick  = { viewModel.startDownload() }
 * )
 *
 * // Custom colours & size
 * CreepButton(
 *     label        = "Get Premium",
 *     buttonColor  = Color(0xFF6C63FF),
 *     baseColor    = Color(0xFF1A1A2E),
 *     width        = 240.dp,
 *     height       = 64.dp,
 *     fontSize     = 22.sp,
 *     onClick      = { openPremium() }
 * )
 * ```
 *
 * @param label         Text displayed on the pill face.
 * @param onClick       Invoked when the user taps/clicks the button.
 * @param modifier      Outer modifier applied to the bounding box.
 * @param buttonColor   Fill colour of the sliding pill (default coral-red).
 * @param baseColor     Colour of the dark base + eyes background.
 * @param labelColor    Text colour on the pill.
 * @param width         Total width of the button widget.
 * @param height        Height of the pill face (base adds extra below).
 * @param cornerRadius  Pill corner radius (default fully-rounded ends).
 * @param fontSize      Label font size.
 * @param fontWeight    Label font weight.
 * @param enabled       When false, no animation or click events fire.
 * @param creepOffset   How many dp the pill lifts on hover/press.
 */
@Composable
fun CreepButton(
    label:        String,
    onClick:      () -> Unit,
    modifier:     Modifier      = Modifier,
    buttonColor:  Color         = ButtonRed,
    baseColor:    Color         = BaseDark,
    labelColor:   Color         = LabelWhite,
    width:        Dp            = 220.dp,
    height:       Dp            = 62.dp,
    cornerRadius: Dp            = 100.dp,          // pill shape
    fontSize:     TextUnit      = 24.sp,
    fontWeight:   FontWeight    = FontWeight.ExtraBold,
    enabled:      Boolean       = true,
    creepOffset:  Dp            = 18.dp,           // how far the pill lifts
) {
    val interactionSource = remember { MutableInteractionSource() }

    // ── Hover (Desktop) + Press (all platforms) ──────────────────────────────
    val isHovered  by interactionSource.collectIsHoveredAsState()
    val isPressed  by interactionSource.collectIsPressedAsState()
    val active      = enabled && (isHovered || isPressed)

    // ── Animated vertical offset of the pill ─────────────────────────────────
    val offsetPx by animateDpAsState(
        targetValue   = if (active) -creepOffset else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "creepLift"
    )

    // ── Eye blink/peek animation: pupils scale up when revealed ──────────────
    val eyeScale by animateFloatAsState(
        targetValue   = if (active) 1f else 0.4f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label         = "eyeScale"
    )

    // ── Eye look-around: subtle horizontal wander ─────────────────────────────
    val inf = rememberInfiniteTransition(label = "eyeWander")
    val eyeWander by inf.animateFloat(
        initialValue  = -1f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeWander"
    )

    // Total widget height = pill height + creepOffset (room for the eyes to peek)
    val totalHeight = height + creepOffset + 6.dp   // 6 dp = base bottom padding

    Box(
        modifier  = modifier
            .width(width)
            .height(totalHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── 1. Dark base (always at bottom, draws eyes) ───────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height + 6.dp)               // slightly taller to show base edge
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(cornerRadius))
                .background(baseColor),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Eyes — peek up when button lifts
            Row(
                modifier              = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    val lookDir = if (idx == 0) -1f else 1f
                    EyeBall(
                        scale    = eyeScale,
                        wanderX  = eyeWander * lookDir * 4f,   // opposite directions
                        eyeWhite = EyeWhite,
                        pupil    = EyePupil,
                        size     = 22.dp
                    )
                }
            }
        }

        // ── 2. Red pill (slides up) ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = offsetPx)
                .align(Alignment.TopCenter)
                .shadow(
                    elevation      = if (active) 2.dp else 6.dp,
                    shape          = RoundedCornerShape(cornerRadius),
                    ambientColor   = baseColor.copy(alpha = 0.3f),
                    spotColor      = baseColor.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(if (enabled) buttonColor else buttonColor.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = interactionSource,
                    indication        = null,        // custom animation; no ripple
                    enabled           = enabled,
                    onClick           = onClick
                )
                .hoverable(interactionSource, enabled = enabled),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                color      = labelColor,
                fontSize   = fontSize,
                fontWeight = fontWeight,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// ── Eyeball sub-composable ───────────────────────────────────────────────────

/**
 * A simple animated eyeball — white sclera with a dark pupil that wanders
 * horizontally based on [wanderX].
 *
 * @param scale     0 → collapsed (not visible), 1 → fully open.
 * @param wanderX   Pixel offset of the pupil along the X axis.
 * @param size      Diameter of the entire eyeball.
 */
@Composable
private fun EyeBall(
    scale:     Float,
    wanderX:   Float,
    eyeWhite:  Color,
    pupil:     Color,
    size:      Dp
) {
    val px = size
    Box(
        modifier         = Modifier
            .size(px)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(eyeWhite),
        contentAlignment = Alignment.Center
    ) {
        // Pupil
        Box(
            modifier = Modifier
                .size(px * 0.42f)
                .offset(x = (wanderX * 0.3f).dp)
                .clip(CircleShape)
                .background(pupil)
        )
    }
}

// ── Convenience variant that accepts an icon slot ───────────────────────────

/**
 * Extended variant of [CreepButton] that accepts a leading [icon] composable
 * (e.g. a Material icon) placed to the left of the label.
 *
 * ```kotlin
 * CreepButton(
 *     label  = "Download",
 *     icon   = { Icon(Icons.Default.Download, contentDescription = null, tint = Color.White) },
 *     onClick = { … }
 * )
 * ```
 */
@Composable
fun CreepButton(
    label:       String,
    onClick:     () -> Unit,
    icon:        @Composable RowScope.() -> Unit,
    modifier:    Modifier   = Modifier,
    buttonColor: Color      = ButtonRed,
    baseColor:   Color      = BaseDark,
    labelColor:  Color      = LabelWhite,
    width:       Dp         = 240.dp,
    height:      Dp         = 62.dp,
    cornerRadius:Dp         = 100.dp,
    fontSize:    TextUnit   = 22.sp,
    fontWeight:  FontWeight = FontWeight.ExtraBold,
    enabled:     Boolean    = true,
    creepOffset: Dp         = 18.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val active     = enabled && (isHovered || isPressed)

    val offsetPx by animateDpAsState(
        targetValue   = if (active) -creepOffset else 0.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "creepLiftIcon"
    )
    val eyeScale by animateFloatAsState(
        targetValue   = if (active) 1f else 0.4f,
        animationSpec = spring(0.6f, Spring.StiffnessLow),
        label         = "eyeScaleIcon"
    )
    val inf = rememberInfiniteTransition(label = "wanderIcon")
    val eyeWander by inf.animateFloat(
        initialValue  = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "eyeWanderIcon"
    )

    val totalHeight = height + creepOffset + 6.dp

    Box(modifier = modifier.width(width).height(totalHeight), contentAlignment = Alignment.BottomCenter) {
        // Base
        Box(
            modifier = Modifier
                .fillMaxWidth().height(height + 6.dp).align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(cornerRadius)).background(baseColor),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    EyeBall(eyeScale, eyeWander * (if (idx == 0) -1f else 1f) * 4f, EyeWhite, EyePupil, 22.dp)
                }
            }
        }
        // Pill
        Box(
            modifier = Modifier
                .fillMaxWidth().height(height)
                .offset(y = offsetPx).align(Alignment.TopCenter)
                .shadow(if (active) 2.dp else 6.dp, RoundedCornerShape(cornerRadius),
                    ambientColor = baseColor.copy(0.3f), spotColor = baseColor.copy(0.4f))
                .clip(RoundedCornerShape(cornerRadius))
                .background(if (enabled) buttonColor else buttonColor.copy(0.5f))
                .clickable(interactionSource, null, enabled, onClick = onClick)
                .hoverable(interactionSource, enabled),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon()
                Text(label, color = labelColor, fontSize = fontSize, fontWeight = fontWeight, letterSpacing = 1.sp)
            }
        }
    }
}
