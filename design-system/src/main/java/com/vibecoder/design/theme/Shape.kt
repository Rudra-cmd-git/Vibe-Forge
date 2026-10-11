package com.vibecoder.design.theme

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Vibe Coder Shape System
 *
 * 16-20dp corner radius, pill-shaped inputs, 8dp grid.
 * 1dp hairline borders instead of heavy shadows.
 * Subtle blur/glass only on bottom sheets and top bar.
 */
object VibeShapes {

    // ===== CORNER RADII =====
    // Extra Small - 4dp (chips, badges, small buttons)
    val ExtraSmall = RoundedCornerShape(4.dp)

    // Small - 8dp (buttons, cards, text fields)
    val Small = RoundedCornerShape(8.dp)

    // Medium - 12dp (dialogs, bottom sheets, larger cards)
    val Medium = RoundedCornerShape(12.dp)

    // Large - 16dp (main containers, modals, preview sheet)
    val Large = RoundedCornerShape(16.dp)

    // Extra Large - 20dp (hero elements, onboarding)
    val ExtraLarge = RoundedCornerShape(20.dp)

    // Pill - Fully rounded (input fields, FAB, chips)
    val Pill = RoundedCornerShape(50.dp)

    // Full - Circle (avatars, circular buttons)
    val Circle = RoundedCornerShape(50.dp)

    // ===== MATERIAL 3 SHAPES =====
    val MaterialShapes = Shapes(
        extraSmall = ExtraSmall,
        small = Small,
        medium = Medium,
        large = Large,
        extraLarge = ExtraLarge
    )

    // ===== CUSTOM SHAPES FOR SPECIFIC COMPONENTS =====

    // Chat message bubble - subtle, not round
    val ChatBubble = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = 16.dp, bottomEnd = 4.dp
    )

    val ChatBubbleUser = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = 4.dp, bottomEnd = 16.dp
    )

    // Tool chip - pill with slight flatten
    val ToolChip = RoundedCornerShape(12.dp)

    // Preview sheet handle
    val SheetHandle = RoundedCornerShape(2.dp)

    // Code block
    val CodeBlock = RoundedCornerShape(8.dp)

    // File tree item
    val FileTreeItem = RoundedCornerShape(6.dp)

    // Floating action button
    val Fab = RoundedCornerShape(16.dp)

    // Top app bar / bottom bar
    val TopBar = RoundedCornerShape(
        bottomStart = 0.dp, bottomEnd = 0.dp,
        topStart = 0.dp, topEnd = 0.dp
    )

    // Bottom sheet - rounded top only
    val BottomSheet = RoundedCornerShape(
        topStart = 20.dp, topEnd = 20.dp,
        bottomStart = 0.dp, bottomEnd = 0.dp
    )

    // Modal dialog
    val Modal = RoundedCornerShape(20.dp)

    // Dropdown menu
    val Dropdown = RoundedCornerShape(12.dp)

    // Tooltip
    val Tooltip = RoundedCornerShape(8.dp)
}

/**
 * Border specifications
 * 1dp hairline borders instead of heavy shadows
 */
object VibeBorders {
    val Hairline = 1.dp
    val Subtle = 1.dp
    val Emphasis = 2.dp

    // Accent border for focus states
    val AccentFocus = 2.dp

    // Glass border
    val Glass = 1.dp
}

/**
 * Elevation specifications
 * Subtle, layered elevations using surface tint
 */
object VibeElevation {
    val Level0 = 0.dp   // Background
    val Level1 = 1.dp   // Cards, surface
    val Level2 = 4.dp   // Elevated cards, dropdowns
    val Level3 = 8.dp   // Bottom sheets, modals
    val Level4 = 16.dp  // Full-screen modals, navigation drawers
    val Level5 = 24.dp  // Toasts, critical overlays
}

/**
 * Spacing system - 8dp grid
 */
object VibeSpacing {
    val Xxs = 2.dp
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 16.dp
    val Lg = 24.dp
    val Xl = 32.dp
    val Xxl = 48.dp
    val Xxxl = 64.dp

    // Screen padding
    val ScreenHorizontal = 20.dp
    val ScreenVertical = 16.dp

    // Component internal padding
    val ComponentXs = 4.dp
    val ComponentSm = 8.dp
    val ComponentMd = 12.dp
    val ComponentLg = 16.dp
    val ComponentXl = 20.dp
}

/**
 * Animation durations and easings
 * Spring animations (200-300ms)
 */
object VibeMotion {
    // Durations
    val Fast = 150
    val Normal = 250
    val Slow = 350
    val VerySlow = 500

    // Spring specs
    val SpringFast = androidx.compose.animation.core.spring(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
    )

    val SpringNormal = androidx.compose.animation.core.spring(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
    )

    val SpringSlow = androidx.compose.animation.core.spring(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioHighBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
    )

    // Easings
    val EaseOut = androidx.compose.animation.core.Easing.OutExpo
    val EaseInOut = androidx.compose.animation.core.Easing.InOutCubic
    val EaseOutCubic = androidx.compose.animation.core.Easing.OutCubic
}