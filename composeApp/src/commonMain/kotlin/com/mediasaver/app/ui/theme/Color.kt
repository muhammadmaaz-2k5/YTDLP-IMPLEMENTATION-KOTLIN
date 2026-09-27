package com.mediasaver.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Bento Pastel Design System (matches reference UI mockup) ──────────────────

// Canvas / Background
val BentoBackgroundLight = Color(0xFFF7F8FC)   // Crisp soft periwinkle-grey backdrop
val BentoBackgroundDark  = Color(0xFF0F1017)   // Deep refined dark backdrop
val BentoCardWhite       = Color(0xFFFFFFFF)   // Pure white elevated card surface
val BentoCardDark        = Color(0xFF181922)   // Deep slate elevated card surface

// 1. Pastel Sage Green (like "Living Room" & "Light" cards)
val BentoSageContainer     = Color(0xFFDEF0D8) // Soft mint-sage
val BentoSageContainerDark = Color(0xFF1B2E19)
val BentoSageText          = Color(0xFF1E311B)
val BentoSageSubtext       = Color(0xFF536A4F)

// 2. Pastel Sky Blue / Periwinkle (like "AC" device card)
val BentoSkyContainer     = Color(0xFFD7E3FC) // Soft calm blue/periwinkle
val BentoSkyContainerDark = Color(0xFF162544)
val BentoSkyText          = Color(0xFF17284E)
val BentoSkySubtext       = Color(0xFF4C608A)

// 3. Pastel Lavender / Mauve (like "Bed Room" & "Office Room" cards)
val BentoLavenderContainer     = Color(0xFFEBE7F7) // Soft lilac lavender
val BentoLavenderContainerDark = Color(0xFF261D3B)
val BentoLavenderText          = Color(0xFF2B1D4C)
val BentoLavenderSubtext       = Color(0xFF655685)

// 4. Pastel Warm Peach / Amber
val BentoPeachContainer     = Color(0xFFFDE8DF) // Soft peach
val BentoPeachContainerDark = Color(0xFF381F15)
val BentoPeachText          = Color(0xFF4C2515)
val BentoPeachSubtext       = Color(0xFF8A5A46)

// Rose / Error Alert
val BentoRoseContainer      = Color(0xFFFEE2E2)
val BentoRoseContainerDark  = Color(0xFF382026)
val BentoRoseText           = Color(0xFFDC2626)
val BentoRoseSubtext        = Color(0xFF991B1B)

// Amber / Warning Alert
val BentoAmberContainer     = Color(0xFFFEF3C7)
val BentoAmberContainerDark = Color(0xFF3D2E14)
val BentoAmberText          = Color(0xFFD97706)
val BentoAmberSubtext       = Color(0xFF92400E)

// 5. High-Contrast Dark Card with Fluid Waves (like "Add New Devices" card)
val BentoDarkCardBg = Color(0xFF16161D)        // Rich obsidian card background
val BentoWavePurple = Color(0xFF5B43EE)        // Fluid organic wave - vibrant violet
val BentoWaveBlue   = Color(0xFFAEC4F8)        // Fluid organic wave - soft periwinkle

// 6. Primary Action & Switch Accent (like the vibrant purple "ON" switches & chart bar)
val BentoPurplePrimary   = Color(0xFF5842ED)   // Signature vibrant purple
val BentoPurpleHover     = Color(0xFF4733D9)
val BentoPurpleContainer = Color(0xFFEBE8FE)   // Tinted purple container
val BentoPurpleDark      = Color(0xFF7B68FF)

// 7. Neutral Typography & Borders
val BentoTextPrimaryLight   = Color(0xFF111218) // Main titles & strong text
val BentoTextPrimaryDark    = Color(0xFFF4F5FB)
val BentoTextSecondaryLight = Color(0xFF8F91A4) // "Hello," & subtle subheads
val BentoTextSecondaryDark  = Color(0xFF8B8EAA)
val BentoBorderLight        = Color(0xFFEAEBF3) // Subtle ambient borders
val BentoBorderDark         = Color(0xFF262837)

// ── Legacy Compatibility Tokens ───────────────────────────────────────────────
val Brand400 = BentoPurpleDark
val Brand500 = BentoPurplePrimary
val Brand600 = BentoPurpleHover
val Brand200 = BentoPurpleContainer

val Accent400 = Color(0xFF38D79F)
val Accent600 = Color(0xFF00A383)

val Surface10 = BentoBackgroundDark
val Surface15 = BentoCardDark
val Surface20 = Color(0xFF1E202C)
val Surface30 = Color(0xFF282A3A)

val OnSurfaceDim = BentoTextSecondaryDark
val Outline      = BentoBorderDark

val ErrorRed     = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF34C759)
val WarningAmber = Color(0xFFFFB800)

val GradientStart = BentoPurplePrimary
val GradientEnd   = Color(0xFF8E7CFF)

val OnboardingPurpleTop = BentoPurplePrimary
val PremiumLavender     = BentoLavenderContainer
val PremiumLavenderDark = BentoLavenderContainerDark

// Platform brand accents
val YouTubeChip   = Color(0xFFFF0000)
val InstagramChipStart = Color(0xFFF9CE34)
val InstagramChipMid   = Color(0xFFEE2A7B)
val InstagramChipEnd   = Color(0xFF6228D7)
val FacebookChip  = Color(0xFF1877F2)
val XChip         = Color(0xFF111218)
val PinterestChip = Color(0xFFE60023)

