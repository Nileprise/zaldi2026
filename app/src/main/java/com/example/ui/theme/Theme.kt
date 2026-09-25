package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberContainer,
    onPrimaryContainer = OnAmberContainer,
    secondary = LogisticsBlue,
    onSecondary = Color.White,
    secondaryContainer = LogisticsBlueContainer,
    onSecondaryContainer = OnLogisticsBlueContainer,
    tertiary = AmberPrimaryDark,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    surfaceVariant = SurfaceTertiary,
    onSurfaceVariant = TextMuted,
    outline = BorderLight,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = LogisticsBlue,
    onSecondary = Color.White,
    secondaryContainer = OnLogisticsBlueContainer,
    onSecondaryContainer = LogisticsBlueContainer,
    background = SurfaceInverse,
    onBackground = Color(0xFFF9F9FB),
    surface = Color(0xFF1E2024),
    onSurface = Color(0xFFF9F9FB),
    surfaceVariant = Color(0xFF282A30),
    onSurfaceVariant = Color(0xFFB0B4C0),
    outline = Color(0xFF383A42),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand aesthetic cohesive
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
