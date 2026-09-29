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

private val ZaldiDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = AmberPrimary,
    secondary = SkyAzure,
    onSecondary = White,
    secondaryContainer = Slate800,
    onSecondaryContainer = SkyLight,
    tertiary = EmeraldAccent,
    onTertiary = White,
    background = Slate900,
    onBackground = White,
    surface = Slate800,
    onSurface = White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    error = RedDanger,
    onError = White
)

private val ZaldiLightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Slate900,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = AmberDark,
    secondary = SkyAzure,
    onSecondary = White,
    tertiary = EmeraldAccent,
    onTertiary = White,
    background = Slate900, // Keep sleek dark logistics palette
    onBackground = White,
    surface = Slate800,
    onSurface = White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded logistics color scheme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZaldiDarkColorScheme else ZaldiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
