package com.snehil.moon_stays_androidapp.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Moonlight Stays design tokens — canonical source: design-system/tokens.json
// Keep in sync with web: moonlight-stays/src/app/globals.css
// ============================================================================

// color.dark.background — midnight scale
val Midnight950 = Color(0xFF0A0A1A)
val Midnight900 = Color(0xFF0F0F23)
val Midnight800 = Color(0xFF15152E)
val Midnight700 = Color(0xFF1A1A3E)

// color.dark.accent
val AccentCyan = Color(0xFF00FFFF)
val AccentCoral = Color(0xFFFF7F50)
val OnAccent = Midnight950

// color.dark.glass — translucent white surfaces over midnight
val GlassSurface = Color(0x0DFFFFFF)        // 5% white
val GlassSurfaceStrong = Color(0x14FFFFFF)  // 8% white
val GlassSurfaceHover = Color(0x1AFFFFFF)   // 10% white
val GlassBorder = Color(0x1AFFFFFF)         // 10% white
val GlassBorderStrong = Color(0x33FFFFFF)   // 20% white
val Scrim = Color(0x99000000)               // 60% black

// color.dark.text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)       // 70% white
val TextMuted = Color(0x80FFFFFF)           // 50% white
val TextDisabled = Color(0x61FFFFFF)        // 38% white

// color.dark.semantic
val Success = Color(0xFF00E479)
val Warning = Color(0xFFFFC857)
val ErrorRed = Color(0xFFFF4D6D)
val Rating = Color(0xFFFFC857)

// ============================================================================
// Legacy aliases — pre-design-system names still referenced by mainui screens.
// Values are remapped onto the canonical tokens; prefer the tokens above.
// ============================================================================
val MoonSurface = Midnight950
val MoonPrimary = TextPrimary
val MoonPrimaryFixedDim = AccentCyan
val MoonSecondary = AccentCoral
val MoonSecondaryContainer = Color(0xFF8E2C01)
val MoonOnSurfaceVariant = TextSecondary
val MoonSurfaceContainerHighest = Midnight700
val MoonSurfaceContainer = Midnight900
val MoonOnSurface = TextPrimary

val MoonSurfaceNocturnal = Midnight900
val MoonSurfaceLunar = Midnight800
val MoonSurfaceTwilight = Midnight700

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
