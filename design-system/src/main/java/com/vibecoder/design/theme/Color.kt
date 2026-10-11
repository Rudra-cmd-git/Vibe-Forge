package com.vibecoder.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Vibe Coder Color Palette
 *
 * Dark-first design inspired by Linear, Arc, Raycast.
 * No pure black/white, no rainbow gradients.
 * One accent only: soft indigo-violet #8B7CFF
 */
object VibeColors {

    // ===== DARK THEME (Primary) =====
    // Background layers - deep, calm, not pure black
    val DarkBackground = Color(0xFF0B0B0F)      // Main background
    val DarkSurface = Color(0xFF14141A)         // Cards, sheets, panels
    val DarkSurfaceElevated = Color(0xFF1C1C24) // Elevated cards, dropdowns, modals
    val DarkBorder = Color(0xFF26262F)          // Hairline borders, dividers

    // Text - high contrast but not harsh
    val DarkText = Color(0xFFECECF1)            // Primary text
    val DarkTextMuted = Color(0xFF8A8A99)       // Secondary text, placeholders, timestamps
    val DarkTextSubtle = Color(0xFF5A5A6E)      // Disabled, very subtle text

    // Accent - the ONE brand color
    val DarkAccent = Color(0xFF8B7CFF)          // Buttons, active states, focus glow, links
    val DarkAccentContainer = Color(0xFF2A2440) // Accent backgrounds (10% opacity)
    val DarkAccentHover = Color(0xFF9D92FF)     // Hover/focus state
    val DarkAccentPress = Color(0xFF7A6BE6)     // Pressed state

    // Semantic - used sparingly
    val DarkSuccess = Color(0xFF4ADE9A)         // Success states, completed tasks
    val DarkSuccessContainer = Color(0xFF1A3A2A)
    val DarkError = Color(0xFFFF6B6B)           // Errors, destructive actions
    val DarkErrorContainer = Color(0xFF3A1A1A)
    val DarkWarning = Color(0xFFFFD166)         // Warnings
    val DarkWarningContainer = Color(0xFF3A3010)

    // Overlay / Glass
    val DarkOverlay = Color(0xCC000000)         // Bottom sheet backdrop (80%)
    val DarkGlass = Color(0x8014141A)           // Glass morphism (50%)
    val DarkGlassBorder = Color(0x338B7CFF)     // Subtle accent border on glass

    // ===== LIGHT THEME (Optional) =====
    val LightBackground = Color(0xFFFAFAFC)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceElevated = Color(0xFFF4F4F8)
    val LightBorder = Color(0xFFE0E0E8)

    val LightText = Color(0xFF1A1A2E)
    val LightTextMuted = Color(0xFF6B6B7D)
    val LightTextSubtle = Color(0xFF9A9AB0)

    val LightAccent = Color(0xFF6B5CE8)
    val LightAccentContainer = Color(0xFFF0EEFF)
    val LightAccentHover = Color(0xFF5A4BD8)
    val LightAccentPress = Color(0xFF4A3BC8)

    val LightSuccess = Color(0xFF2EB872)
    val LightSuccessContainer = Color(0xFFE8FFF0)
    val LightError = Color(0xFFE03E3E)
    val LightErrorContainer = Color(0xFFFFE8E8)
    val LightWarning = Color(0xFFE8A800)
    val LightWarningContainer = Color(0xFFFFF4E0)

    val LightOverlay = Color(0xCC000000)
    val LightGlass = Color(0x80FFFFFF)
    val LightGlassBorder = Color(0x336B5CE8)

    // ===== COLOR SCHEMES =====
    val DarkColorScheme = darkColorScheme(
        // Primary - our accent
        primary = DarkAccent,
        onPrimary = Color.White,
        primaryContainer = DarkAccentContainer,
        onPrimaryContainer = DarkAccent,

        // Secondary - muted accent for less emphasis
        secondary = DarkAccent.copy(alpha = 0.7f),
        onSecondary = Color.White,
        secondaryContainer = DarkAccentContainer.copy(alpha = 0.5f),
        onSecondaryContainer = DarkAccent,

        // Tertiary - success
        tertiary = DarkSuccess,
        onTertiary = Color.Black,
        tertiaryContainer = DarkSuccessContainer,
        onTertiaryContainer = DarkSuccess,

        // Error
        error = DarkError,
        onError = Color.White,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkError,

        // Background & Surface
        background = DarkBackground,
        onBackground = DarkText,
        surface = DarkSurface,
        onSurface = DarkText,
        surfaceVariant = DarkSurfaceElevated,
        onSurfaceVariant = DarkTextMuted,

        // Outline
        outline = DarkBorder,
        outlineVariant = DarkBorder.copy(alpha = 0.5f),

        // Inverse
        inverseSurface = LightSurface,
        inverseOnSurface = LightText,
        inversePrimary = LightAccent,

        // Shadow
        shadow = Color.Black,
        scrim = Color.Black,

        // Surface tint (Material 3 elevation overlay)
        surfaceTint = DarkAccent
    )

    val LightColorScheme = lightColorScheme(
        primary = LightAccent,
        onPrimary = Color.White,
        primaryContainer = LightAccentContainer,
        onPrimaryContainer = LightAccent,

        secondary = LightAccent.copy(alpha = 0.7f),
        onSecondary = Color.White,
        secondaryContainer = LightAccentContainer.copy(alpha = 0.5f),
        onSecondaryContainer = LightAccent,

        tertiary = LightSuccess,
        onTertiary = Color.White,
        tertiaryContainer = LightSuccessContainer,
        onTertiaryContainer = LightSuccess,

        error = LightError,
        onError = Color.White,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightError,

        background = LightBackground,
        onBackground = LightText,
        surface = LightSurface,
        onSurface = LightText,
        surfaceVariant = LightSurfaceElevated,
        onSurfaceVariant = LightTextMuted,

        outline = LightBorder,
        outlineVariant = LightBorder.copy(alpha = 0.5f),

        inverseSurface = DarkSurface,
        inverseOnSurface = DarkText,
        inversePrimary = DarkAccent,

        shadow = Color.Black,
        scrim = Color.Black,

        surfaceTint = LightAccent
    )

    // ===== SEMANTIC COLORS FOR DIRECT USE =====
    @Composable
    fun rememberSemanticColors(isDark: Boolean = true): SemanticColors {
        return remember { SemanticColors(isDark) }
    }
}

/**
 * Semantic color tokens for direct use in components.
 * Use these instead of ColorScheme when you need specific semantic meanings.
 */
data class SemanticColors(
    val isDark: Boolean
) {
    // Background layers
    val background: Color = if (isDark) VibeColors.DarkBackground else VibeColors.LightBackground
    val surface: Color = if (isDark) VibeColors.DarkSurface else VibeColors.LightSurface
    val surfaceElevated: Color = if (isDark) VibeColors.DarkSurfaceElevated else VibeColors.LightSurfaceElevated
    val border: Color = if (isDark) VibeColors.DarkBorder else VibeColors.LightBorder

    // Text
    val text: Color = if (isDark) VibeColors.DarkText else VibeColors.LightText
    val textMuted: Color = if (isDark) VibeColors.DarkTextMuted else VibeColors.LightTextMuted
    val textSubtle: Color = if (isDark) VibeColors.DarkTextSubtle else VibeColors.LightTextSubtle

    // Accent
    val accent: Color = if (isDark) VibeColors.DarkAccent else VibeColors.LightAccent
    val accentContainer: Color = if (isDark) VibeColors.DarkAccentContainer else VibeColors.LightAccentContainer
    val accentHover: Color = if (isDark) VibeColors.DarkAccentHover else VibeColors.LightAccentHover
    val accentPress: Color = if (isDark) VibeColors.DarkAccentPress else VibeColors.LightAccentPress

    // Semantic
    val success: Color = if (isDark) VibeColors.DarkSuccess else VibeColors.LightSuccess
    val successContainer: Color = if (isDark) VibeColors.DarkSuccessContainer else VibeColors.LightSuccessContainer
    val error: Color = if (isDark) VibeColors.DarkError else VibeColors.LightError
    val errorContainer: Color = if (isDark) VibeColors.DarkErrorContainer else VibeColors.LightErrorContainer
    val warning: Color = if (isDark) VibeColors.DarkWarning else VibeColors.LightWarning
    val warningContainer: Color = if (isDark) VibeColors.DarkWarningContainer else VibeColors.LightWarningContainer

    // Overlay / Glass
    val overlay: Color = if (isDark) VibeColors.DarkOverlay else VibeColors.LightOverlay
    val glass: Color = if (isDark) VibeColors.DarkGlass else VibeColors.LightGlass
    val glassBorder: Color = if (isDark) VibeColors.DarkGlassBorder else VibeColors.LightGlassBorder
}

/**
 * Extension for easy access to semantic colors in composables
 */
@Composable
fun semanticColors(): SemanticColors {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.isDark
    return VibeColors.rememberSemanticColors(isDark)
}