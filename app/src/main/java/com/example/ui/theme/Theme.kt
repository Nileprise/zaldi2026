package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue900,
    
    secondary = Slate500,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,

    background = Slate50,
    onBackground = Slate900,
    
    surface = Color.White,
    onSurface = Slate900,
    
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    
    outline = Slate200,
    outlineVariant = Slate200.copy(alpha = 0.5f),
    
    error = Red600,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red800
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Slate900,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue100,
    
    secondary = Slate500,
    onSecondary = Slate900,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate100,

    background = Slate900,
    onBackground = Slate50,
    
    surface = Slate800,
    onSurface = Slate50,
    
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    
    outline = Slate500,
    outlineVariant = Slate700,
    
    error = Red300,
    onError = Slate900,
    errorContainer = Red800,
    onErrorContainer = Red100
)

@Composable
fun AkhilLogisticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (API 31+)
    dynamicColor: Boolean = false, // Set to true to allow Android to override with wallpaper colors
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

    // Modern Android Window Inset & Status Bar configuration
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure you have a Typography.kt defined
        content = content
    )
}
