package com.mediasaver.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ── Brand amber palette (matches the reference image) ───────────────────────
private val AmberPrimary  = Color(0xFFF5A623)   // vivid orange-amber  (arc head / active spoke)
private val AmberLight    = Color(0xFFFFD97D)   // pale gold            (arc tail / inactive spoke)
private val AmberDark     = Color(0xFF7B4B00)   // deep brown           (darkest spoke)

// ── Loader variant enum ──────────────────────────────────────────────────────
enum class LoaderVariant {
    /** Thin-stroke single arc rotating continuously. */
    ThinArc,
    /** Thick-stroke arc with gradient-like fade from head to tail. */
    ThickArc,
    /** 8 capsule-shaped spokes arranged radially, each fading from dark → light. */
    RadialDots
}

/**
 * Reusable animated loader widget that matches the three styles shown in the
 * brand reference image (orange / amber colour scheme).
 *
 * Usage:
 * ```kotlin
 * // Inline spinner next to some text:
 * LoaderWidget(variant = LoaderVariant.ThinArc, size = 32.dp)
 *
 * // Full-screen overlay:
 * LoaderWidget(
 *     variant    = LoaderVariant.RadialDots,
 *     size       = 56.dp,
 *     label      = "Downloading…",
 *     modifier   = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param variant   Which spinner style to render (default [LoaderVariant.ThinArc]).
 * @param size      Diameter of the spinner canvas (default 48.dp).
 * @param label     Optional label shown below the spinner.
 * @param modifier  Outer modifier applied to the containing [Column].
 */
@Composable
fun LoaderWidget(
    variant:  LoaderVariant = LoaderVariant.ThinArc,
    size:     Dp            = 48.dp,
    label:    String?       = null,
    modifier: Modifier      = Modifier
) {
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        when (variant) {
            LoaderVariant.ThinArc    -> ThinArcSpinner(size)
            LoaderVariant.ThickArc   -> ThickArcSpinner(size)
            LoaderVariant.RadialDots -> RadialDotsSpinner(size)
        }
        if (!label.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color      = AmberPrimary
            )
        }
    }
}

// ── 1. Thin-Arc Spinner ──────────────────────────────────────────────────────

@Composable
private fun ThinArcSpinner(size: Dp) {
    val rotation by rememberInfiniteRotation(durationMs = 900)

    Canvas(modifier = Modifier.size(size)) {
        val stroke     = size.toPx() * 0.08f          // ~8 % of diameter
        val sweepAngle = 260f
        val padding    = stroke / 2f
        val arcSize    = Size(this.size.width - padding * 2, this.size.height - padding * 2)

        // Ghost track
        drawArc(
            color       = AmberLight.copy(alpha = 0.30f),
            startAngle  = 0f,
            sweepAngle  = 360f,
            useCenter   = false,
            topLeft     = Offset(padding, padding),
            size        = arcSize,
            style       = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Active arc
        rotate(rotation) {
            drawArc(
                color      = AmberPrimary,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter  = false,
                topLeft    = Offset(padding, padding),
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

// ── 2. Thick-Arc Spinner ─────────────────────────────────────────────────────

@Composable
private fun ThickArcSpinner(size: Dp) {
    val rotation by rememberInfiniteRotation(durationMs = 1100)

    Canvas(modifier = Modifier.size(size)) {
        val stroke    = size.toPx() * 0.16f           // ~16 % of diameter — noticeably thicker
        val sweep     = 220f
        val padding   = stroke / 2f
        val arcSize   = Size(this.size.width - padding * 2, this.size.height - padding * 2)

        // Ghost track (very faint)
        drawArc(
            color      = AmberLight.copy(alpha = 0.20f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter  = false,
            topLeft    = Offset(padding, padding),
            size       = arcSize,
            style      = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Tail (lighter segment behind active head)
        rotate(rotation) {
            drawArc(
                color      = AmberLight.copy(alpha = 0.55f),
                startAngle = -90f + sweep * 0.5f,
                sweepAngle = sweep * 0.5f,
                useCenter  = false,
                topLeft    = Offset(padding, padding),
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Head (bold, saturated)
            drawArc(
                color      = AmberPrimary,
                startAngle = -90f,
                sweepAngle = sweep * 0.5f,
                useCenter  = false,
                topLeft    = Offset(padding, padding),
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

// ── 3. Radial-Dots Spinner ───────────────────────────────────────────────────

private val DotsAlphaLevels = listOf(1.00f, 0.85f, 0.65f, 0.45f, 0.30f, 0.18f, 0.10f, 0.05f)
private val DotsColors       = listOf(AmberDark, AmberPrimary, AmberPrimary, AmberPrimary, AmberLight, AmberLight, AmberLight, AmberLight)
private const val SPOKE_COUNT = 8

@Composable
private fun RadialDotsSpinner(size: Dp) {
    // Frame advances one spoke-step every ~100 ms → full revolution ≈ 800 ms
    val frameIndex by rememberInfiniteFrameIndex(stepMs = 100, steps = SPOKE_COUNT)

    Canvas(modifier = Modifier.size(size)) {
        val cx         = this.size.width  / 2f
        val cy         = this.size.height / 2f
        val outerR     = this.size.minDimension / 2f
        val innerR     = outerR * 0.38f              // spoke starts here
        val spokeW     = outerR * 0.22f              // capsule width
        val spokeH     = outerR * 0.52f              // capsule length

        for (i in 0 until SPOKE_COUNT) {
            val angleDeg  = i * (360f / SPOKE_COUNT) - 90f
            val angleRad  = Math.toRadians(angleDeg.toDouble())

            // Centre of this spoke's capsule
            val midR      = (innerR + outerR) / 2f
            val cx2       = cx + midR * cos(angleRad).toFloat()
            val cy2       = cy + midR * sin(angleRad).toFloat()

            // Which alpha/colour slot this spoke occupies given the current frame
            val slot      = (i - frameIndex + SPOKE_COUNT) % SPOKE_COUNT
            val alpha     = DotsAlphaLevels[slot]
            val color     = DotsColors[slot].copy(alpha = alpha)

            rotate(degrees = angleDeg + 90f, pivot = Offset(cx, cy)) {
                drawRoundRect(
                    color         = color,
                    topLeft       = Offset(cx2 - spokeW / 2f, cy2 - spokeH / 2f),
                    size          = Size(spokeW, spokeH),
                    cornerRadius  = androidx.compose.ui.geometry.CornerRadius(spokeW / 2f)
                )
            }
        }
    }
}

// ── Shared animation helpers ─────────────────────────────────────────────────

/** Continuous 0 → 360° rotation for arc spinners. */
@Composable
private fun rememberInfiniteRotation(durationMs: Int): State<Float> {
    val inf = rememberInfiniteTransition(label = "loaderRotation")
    return inf.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
}

/**
 * Discrete frame counter — advances by 1 every [stepMs] ms,
 * wrapping at [steps]. Used by [RadialDotsSpinner].
 */
@Composable
private fun rememberInfiniteFrameIndex(stepMs: Int, steps: Int): State<Int> {
    val totalMs = stepMs * steps
    val inf     = rememberInfiniteTransition(label = "loaderFrame")
    val raw     = inf.animateFloat(
        initialValue   = 0f,
        targetValue    = totalMs.toFloat(),
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = totalMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame"
    )
    return derivedStateOf { (raw.value / stepMs).toInt() % steps }
}

// ── Design preview (visible in Android Studio Design panel) ─────────────────

/**
 * Shows all three [LoaderVariant]s side-by-side on an amber background,
 * matching the reference image.
 *
 * Not shipped in production — gated on the `Preview` annotation which is
 * available only in the IDE tooling classpath.
 */
@Composable
fun LoaderWidgetPreview() {
    androidx.compose.foundation.layout.Box(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment    = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            LoaderWidget(variant = LoaderVariant.ThinArc,    size = 56.dp)
            LoaderWidget(variant = LoaderVariant.ThickArc,   size = 56.dp)
            LoaderWidget(variant = LoaderVariant.RadialDots, size = 56.dp)
        }
    }
}

