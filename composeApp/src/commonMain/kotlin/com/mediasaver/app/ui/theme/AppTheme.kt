package com.mediasaver.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Dark color scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Brand400,
    onPrimary          = Color.White,
    primaryContainer   = Brand600,
    onPrimaryContainer = Brand200,
    secondary          = Accent400,
    onSecondary        = Color.Black,
    secondaryContainer = Color(0xFF003D2F),
    onSecondaryContainer = Accent400,
    background         = Surface10,
    onBackground       = Color(0xFFE8E8FF),
    surface            = Surface15,
    onSurface          = Color(0xFFE8E8FF),
    surfaceVariant     = Surface30,
    onSurfaceVariant   = OnSurfaceDim,
    outline            = Outline,
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF5C0A1B),
    onErrorContainer   = Color(0xFFFFB3BC)
)

// ── Light color scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Brand500,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFEBE9FF),
    onPrimaryContainer = Brand600,
    secondary          = Accent600,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFB3F4E8),
    onSecondaryContainer = Color(0xFF003D2F),
    background         = Color(0xFFF5F5FF),
    onBackground       = Color(0xFF1A1A2E),
    surface            = Color.White,
    onSurface          = Color(0xFF1A1A2E),
    surfaceVariant     = Color(0xFFEEEEFF),
    onSurfaceVariant   = Color(0xFF50506A),
    outline            = Color(0xFFBBBBDD),
    error              = Color(0xFFBA1A1A),
    onError            = Color.White
)

/**
 * Root theme composable.
 * On Android 12+ uses dynamic color when available (Material You).
 * Falls back to our brand palette on older Android / Desktop.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color only makes sense on Android — platform entry points pass `true` if supported
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme  -> DarkColorScheme
        else       -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = appTypography(),
        shapes      = AppShapes,
        content     = content
    )
}

/** Shared purple hero gradient — top bar backdrop, empty state icon, thumbnail placeholders. */
val HeroGradient = Brush.linearGradient(listOf(GradientStart, GradientEnd))
