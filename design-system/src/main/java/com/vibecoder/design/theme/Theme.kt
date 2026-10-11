package com.vibecoder.design.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Vibe Coder Theme
 *
 * Main entry point for the design system.
 * Dark-first, with optional light theme support.
 */
@Composable
fun VibeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VibeColors.DarkColorScheme else VibeColors.LightColorScheme
    val shapes = VibeShapes.MaterialShapes
    val typography = VibeTypography.MaterialTypography

    // Status bar / navigation bar colors
    SideEffect {
        val context = androidx.compose.ui.platform.LocalContext.current
        val window = (context as? android.app.Activity)?.window
        window?.let { w ->
            val controller = WindowInsetsControllerCompat(w, w.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            w.statusBarColor = colorScheme.background.toArgb()
            w.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = content
    )
}

/**
 * Check if current theme is dark
 */
@Composable
fun isDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.isDark
}

/**
 * Get semantic colors for current theme
 */
@Composable
fun vibeColors(): com.vibecoder.design.theme.SemanticColors {
    return com.vibecoder.design.theme.VibeColors.rememberSemanticColors(isDarkTheme())
}

/**
 * Extension to get current theme's semantic colors
 */
@Composable
fun MaterialTheme.vibeColors(): com.vibecoder.design.theme.SemanticColors {
    return vibeColors()
}