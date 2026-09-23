package com.mediasaver.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Bold, poster-style type scale built on [appFontFamily] — must be called from a composable. */
@Composable
fun appTypography(): Typography {
    val family = appFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.ExtraBold,
            fontSize      = 44.sp,
            lineHeight    = 50.sp,
            letterSpacing = (-1).sp
        ),
        displaySmall = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.ExtraBold,
            fontSize      = 32.sp,
            lineHeight    = 38.sp,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize   = 24.sp,
            lineHeight = 30.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            lineHeight = 26.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.Normal,
            fontSize      = 16.sp,
            lineHeight    = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.Normal,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 12.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.2.sp
        ),
        labelSmall = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 11.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.4.sp
        )
    )
}
