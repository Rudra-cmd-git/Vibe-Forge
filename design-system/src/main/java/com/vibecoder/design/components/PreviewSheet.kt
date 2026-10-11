package com.vibecoder.design.components

import androidx.compose.animation.animateDpAsState
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.dragable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.detectDragGestures
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandFull
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.derivedStateOf

/**
 * PreviewSheet - Draggable bottom sheet (peek / half / full) over chat
 * Toolbar: reload, device size toggle, open in browser, console toggle
 */
@Composable
fun PreviewSheet(
    isVisible: Boolean,
    sheetState: PreviewSheetState,
    onStateChange: (PreviewSheetState) -> Unit,
    onClose: () -> Unit,
    onReload: () -> Unit,
    onDeviceSizeChange: (DeviceSize) -> Unit,
    onOpenInBrowser: () -> Unit,
    onToggleConsole: () -> Unit,
    currentDeviceSize: DeviceSize,
    showConsole: Boolean,
    consoleContent: @Composable () -> Unit,
    previewContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    val height = remember { mutableStateOf(0f) }

    if (!isVisible) return

    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlay)
            .clickable(onClick = { if (sheetState == PreviewSheetState.Peek) onClose() })
            .alpha(if (sheetState == PreviewSheetState.Full) 0.5f else 0.3f)
    )

    // Sheet
    val sheetHeight by animateDpAsState(
        targetValue = when (sheetState) {
            PreviewSheetState.Peek -> 80.dp
            PreviewSheetState.Half -> (height.value / 2).dp
            PreviewSheetState.Full -> height.value.dp
        },
        animationSpec = Spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val sheetY = height.value - sheetHeight.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .y(sheetY)
            .background(colors.surface, VibeShapes.BottomSheet)
            .border(VibeBorders.Hairline, colors.border, VibeShapes.BottomSheet)
            .graphicsLayer { translationY = 0f }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(top = VibeSpacing.Xs)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {},
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newY = sheetY + dragAmount.y
                                val newState = when {
                                    newY < height.value * 0.25 -> PreviewSheetState.Full
                                    newY < height.value * 0.6 -> PreviewSheetState.Half
                                    else -> PreviewSheetState.Peek
                                }
                                if (newState != sheetState) {
                                    onStateChange(newState)
                                }
                            },
                            onDragEnd = {},
                            onDragCancel = {}
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(colors.border, VibeShapes.SheetHandle)
                )
            }

            // Toolbar
            PreviewToolbar(
                onReload = onReload,
                onDeviceSizeChange = onDeviceSizeChange,
                onOpenInBrowser = onOpenInBrowser,
                onToggleConsole = onToggleConsole,
                onClose = { if (sheetState == PreviewSheetState.Full) onStateChange(PreviewSheetState.Half) else onClose() },
                currentDeviceSize = currentDeviceSize,
                showConsole = showConsole,
                sheetState = sheetState
            )

            // Preview content
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(VibeSpacing.ScreenHorizontal)
            ) {
                if (sheetState != PreviewSheetState.Peek) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background, VibeShapes.Medium)
                            .border(VibeBorders.Hairline, colors.border, VibeShapes.Medium)
                            .clip(VibeShapes.Medium)
                    ) {
                        previewContent()
                    }
                }
            }

            // Console
            if (showConsole && sheetState != PreviewSheetState.Peek) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(colors.background, VibeShapes.CodeBlock)
                        .border(VibeBorders.Hairline, colors.border, VibeShapes.CodeBlock)
                        .padding(VibeSpacing.ScreenHorizontal, 0, VibeSpacing.ScreenHorizontal, VibeSpacing.ScreenVertical)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Console", style = VibeTypography.LabelMedium, color = colors.textMuted)
                            IconButton(onClick = onToggleConsole) {
                                Icon(Icons.Filled.Close, "Close console", tint = colors.textMuted)
                            }
                        }
                        consoleContent()
                    }
                }
            }
        }
    }
}

/**
 * Preview toolbar
 */
@Composable
fun PreviewToolbar(
    onReload: () -> Unit,
    onDeviceSizeChange: (DeviceSize) -> Unit,
    onOpenInBrowser: () -> Unit,
    onToggleConsole: () -> Unit,
    onClose: () -> Unit,
    currentDeviceSize: DeviceSize,
    showConsole: Boolean,
    sheetState: PreviewSheetState
) {
    val colors = vibeColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = VibeSpacing.ScreenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Device size selector
        Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)) {
            DeviceSize.values().forEach { size ->
                val selected = size == currentDeviceSize
                androidx.compose.material3.TextButton(
                    onClick = { onDeviceSizeChange(size) },
                    modifier = Modifier.padding(VibeSpacing.Xs),
                    shape = VibeShapes.Small,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = if (selected) colors.accentContainer else Color.Transparent,
                        contentColor = if (selected) colors.accent else colors.textMuted
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs)) {
                        Icon(size.icon, contentDescription = size.label, tint = if (selected) colors.accent else colors.textMuted, modifier = Modifier.size(16.dp))
                        if (sheetState != PreviewSheetState.Peek) {
                            Text(size.label, style = VibeTypography.LabelSmall, color = if (selected) colors.accent else colors.textMuted)
                        }
                    }
                }
            }
        }

        // Right: Actions
        Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)) {
            IconButton(onClick = onReload, contentDescription = "Reload") {
                Icon(Icons.Filled.Refresh, "Reload", tint = colors.textMuted)
            }
            IconButton(onClick = onOpenInBrowser, contentDescription = "Open in browser") {
                Icon(Icons.Filled.OpenInNew, "Open in browser", tint = colors.textMuted)
            }
            IconButton(onClick = onToggleConsole, contentDescription = if (showConsole) "Hide console" else "Show console") {
                Icon(Icons.Filled.Terminal, "Console", tint = if (showConsole) colors.accent else colors.textMuted)
            }
            if (sheetState == PreviewSheetState.Full) {
                IconButton(onClick = onClose, contentDescription = "Minimize") {
                    Icon(Icons.Filled.Remove, "Minimize", tint = colors.textMuted)
                }
            } else {
                IconButton(onClick = onClose, contentDescription = "Close preview") {
                    Icon(Icons.Filled.Close, "Close", tint = colors.textMuted)
                }
            }
        }
    }
}

/**
 * Device size enum
 */
enum class DeviceSize(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val width: Int, val height: Int) {
    Phone("Phone", Icons.Filled.PhoneAndroid, 390, 844),
    Tablet("Tablet", Icons.Filled.TabletMac, 768, 1024),
    Desktop("Desktop", Icons.Filled.DesktopMac, 1440, 900)
}

/**
 * Preview sheet state
 */
enum class PreviewSheetState {
    Peek,   // Just handle bar visible
    Half,   // Half screen
    Full    // Full screen
}

/**
 * Preview sheet state holder for external control
 */
class PreviewSheetStateHolder(
    initialState: PreviewSheetState = PreviewSheetState.Half
) {
    var state: PreviewSheetState = initialState
        private set

    var isVisible: Boolean = true
        private set

    fun setState(newState: PreviewSheetState) {
        state = newState
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    fun toggle() {
        isVisible = !isVisible
    }
}

/**
 * Simple web view preview using Compose Html (for static preview)
 * For full web preview, we'd use Android WebView in a separate component
 */
@Composable
fun WebPreview(
    url: String,
    deviceSize: DeviceSize,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    // This is a placeholder - real implementation would use Android WebView
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background, VibeShapes.Medium)
            .border(VibeBorders.Hairline, colors.border, VibeShapes.Medium)
            .clip(VibeShapes.Medium),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md)) {
            Icon(Icons.Filled.Web, "Web Preview", tint = colors.textMuted, modifier = Modifier.size(48.dp))
            Text("Web Preview", style = VibeTypography.TitleMedium, color = colors.text)
            Text(url, style = VibeTypography.BodySmall, color = colors.textMuted)
            Text("Full WebView implementation needed", style = VibeTypography.LabelSmall, color = colors.textSubtle)
        }
    }
}

/**
 * Console output component
 */
@Composable
fun ConsoleOutput(
    logs: List<ConsoleLog>,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            contentPadding = VibeSpacing.ComponentSm
        ) {
            items(logs.reversed()) { log ->
                ConsoleLogRow(log = log)
            }
        }
    }
}

@Composable
fun ConsoleLogRow(log: ConsoleLog) {
    val colors = vibeColors()
    val (icon, color) = when (log.level) {
        ConsoleLevel.Info -> Icons.Filled.Info to colors.text
        ConsoleLevel.Warn -> Icons.Filled.Warning to colors.warning
        ConsoleLevel.Error -> Icons.Filled.Error to colors.error
        ConsoleLevel.Debug -> Icons.Filled.BugReport to colors.textMuted
        ConsoleLevel.Log -> Icons.Filled.Terminal to colors.text
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ComponentXs, 0),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = log.timestamp, style = codeStyle(CodeSize.Small), color = colors.textSubtle)
        Icon(icon, contentDescription = log.level.name, tint = color, modifier = Modifier.size(12.dp).padding(top = 2.dp))
        Text(text = log.message, style = codeStyle(CodeSize.Small), color = color, modifier = Modifier.weight(1f), maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

data class ConsoleLog(
    val timestamp: String,
    val level: ConsoleLevel,
    val message: String
)

enum class ConsoleLevel {
    Info, Warn, Error, Debug, Log
}