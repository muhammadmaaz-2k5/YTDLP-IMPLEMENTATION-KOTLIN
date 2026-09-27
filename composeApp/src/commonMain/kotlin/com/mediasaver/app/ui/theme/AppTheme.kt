package com.mediasaver.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = compositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalThemeIsDark.current

// ── Dark color scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = BentoPurpleDark,
    onPrimary          = Color.White,
    primaryContainer   = BentoPurpleHover,
    onPrimaryContainer = BentoPurpleContainer,
    secondary          = BentoSkyContainerDark,
    onSecondary        = BentoSkyText,
    secondaryContainer = BentoSageContainerDark,
    onSecondaryContainer = BentoSageText,
    background         = BentoBackgroundDark,
    onBackground       = BentoTextPrimaryDark,
    surface            = BentoCardDark,
    onSurface          = BentoTextPrimaryDark,
    surfaceVariant     = Color(0xFF222432),
    onSurfaceVariant   = BentoTextSecondaryDark,
    outline            = BentoBorderDark,
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF5C0A1B),
    onErrorContainer   = Color(0xFFFFB3BC)
)

// ── Light color scheme (matches reference UI aesthetic) ──────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = BentoPurplePrimary,
    onPrimary          = Color.White,
    primaryContainer   = BentoPurpleContainer,
    onPrimaryContainer = BentoPurplePrimary,
    secondary          = BentoSageContainer,
    onSecondary        = BentoSageText,
    secondaryContainer = BentoSkyContainer,
    onSecondaryContainer = BentoSkyText,
    background         = BentoBackgroundLight,
    onBackground       = BentoTextPrimaryLight,
    surface            = BentoCardWhite,
    onSurface          = BentoTextPrimaryLight,
    surfaceVariant     = Color(0xFFEFF1F8),
    onSurfaceVariant   = BentoTextSecondaryLight,
    outline            = BentoBorderLight,
    error              = ErrorRed,
    onError            = Color.White
)

/**
 * Root theme composable.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = appTypography(),
            shapes      = AppShapes,
            content     = content
        )
    }
}

/** Shared purple gradient — matching the vibrant Bento Purple accent */
val HeroGradient = Brush.linearGradient(listOf(BentoPurplePrimary, Color(0xFF8B77FF)))

