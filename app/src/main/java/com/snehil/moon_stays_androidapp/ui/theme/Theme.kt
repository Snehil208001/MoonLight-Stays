package com.snehil.moon_stays_androidapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Moonlight Stays "Midnight Glassmorphism" scheme — design-system/DESIGN_SYSTEM.md
// Mirrors the web app: midnight backgrounds, cyan primary accent, coral secondary.
private val MoonDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = OnAccent,
    secondary = AccentCoral,
    onSecondary = OnAccent,
    tertiary = TextPrimary,
    onTertiary = Midnight950,
    background = Midnight950,
    onBackground = TextPrimary,
    surface = Midnight900,
    onSurface = TextPrimary,
    surfaceVariant = Midnight800,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Midnight900,
    surfaceContainerHighest = Midnight700,
    outline = GlassBorderStrong,
    outlineVariant = GlassBorder,
    error = ErrorRed,
    onError = Midnight950,
    scrim = Scrim
)

@Composable
fun MoonStaysAndroidAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to force our custom celestial theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Dark-only for now, matching the web app (html.dark forced).
        // Light tokens exist in design-system/tokens.json for future use.
        else -> MoonDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MoonShapes,
        content = content
    )
}
