package com.snehil.moon_stays_androidapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val MoonDarkColorScheme = darkColorScheme(
    primary = MoonPrimary,
    secondary = MoonSecondary,
    tertiary = MoonPrimaryFixedDim,
    background = MoonSurface,
    surface = MoonSurface,
    onPrimary = MoonSurface,
    onSecondary = MoonSurfaceContainer,
    onBackground = MoonOnSurface,
    onSurface = MoonOnSurface,
    surfaceVariant = MoonSurfaceContainerHighest,
    onSurfaceVariant = MoonOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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

        darkTheme -> MoonDarkColorScheme
        else -> MoonDarkColorScheme // Force MoonDarkColorScheme as default for this design
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}