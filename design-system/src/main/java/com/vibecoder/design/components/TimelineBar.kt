package com.vibecoder.design.components

import androidx.compose.animation.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filed.FileCopy
import androidx.compose.runtime.derivedStateOf

/**
 * TimelineBar - Horizontal version timeline with ticks
 * One tick per agent change, with Undo and "Restore this version"
 */
@Composable
fun TimelineBar(
    versions: List<VersionEntry>,
    currentVersion: Int,
    onVersionClick: (Int) -> Unit,
    onUndo: () -> Unit,
    onRestore: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {}
) {
    val colors = vibeColors()
    val isExpanded = remember { mutableStateOf(expanded) }
    val currentExpanded = expanded || isExpanded.value

    Column(modifier = modifier.animateContentSize()) {
        // Compact timeline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(VibeSpacing.ScreenHorizontal)
                .background(colors.surface, VibeShapes.Medium)
                .border(VibeBorders.Hairline, colors.border, VibeShapes.Medium)
                .clickable(onClick = { isExpanded.value = !isExpanded.value })
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Version indicator
                Icon(Icons.Filled.History, "Timeline", tint = colors.accent, modifier = Modifier.padding(start = VibeSpacing.Sm).size(20.dp))

                // Timeline track with ticks
                Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = VibeSpacing.Sm)) {
                    // Track line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.CenterVertically)
                            .background(colors.border, VibeShapes.Pill)
                    )

                    // Ticks
                    versions.withIndex().forEach { (index, version) ->
                        val isCurrent = index == currentVersion
                        val tickColor = if (isCurrent) colors.accent else if (index < currentVersion) colors.success else colors.border
                        val tickSize = if (isCurrent) 12.dp else 8.dp

                        Box(
                            modifier = Modifier
                                .size(tickSize)
                                .background(tickColor, VibeShapes.Circle)
                                .align(Alignment.CenterVertically)
                                .weight(1f, fill = false)
                                .padding(horizontal = 0.dp)
                                .offset { IntOffset((it.width / versions.size) * index - tickSize.value / 2, 0) }
                        )
                    }
                }

                // Current version label
                Text(
                    text = "v${currentVersion + 1}/${versions.size}",
                    style = VibeTypography.LabelMedium,
                    color = colors.textMuted,
                    modifier = Modifier.padding(end = VibeSpacing.Sm)
                )

                // Expand/collapse
                Icon(
                    imageVector = if (currentExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (currentExpanded) "Collapse timeline" else "Expand timeline",
                    tint = colors.textMuted,
                    modifier = Modifier.padding(end = VibeSpacing.Sm).size(20.dp)
                )
            }
        }

        // Expanded timeline details
        if (currentExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Sm)
                    .background(colors.surface, VibeShapes.Medium)
                    .border(VibeBorders.Hairline, colors.border, VibeShapes.Medium)
                    .animateContentSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(VibeSpacing.ComponentMd)
                        .padding(bottom = VibeSpacing.Xs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version History", style = VibeTypography.TitleSmall, color = colors.text)
                    Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)) {
                        androidx.compose.material3.TextButton(onClick = onUndo) {
                            Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs)) {
                                Icon(Icons.Filled.Undo, "Undo", tint = colors.accent, modifier = Modifier.size(16.dp))
                                Text("Undo", style = VibeTypography.LabelMedium, color = colors.accent)
                            }
                        }
                    }
                }

                // Version list
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(VibeSpacing.ComponentMd).fillMaxWidth(),
                    contentPadding = VibeSpacing.ComponentXs
                ) {
                    items(versions.withIndex().reversed()) { (index, version) ->
                        val isCurrent = index == currentVersion
                        VersionRow(
                            version = version,
                            index = index,
                            isCurrent = isCurrent,
                            onClick = { onVersionClick(index) },
                            onRestore = { onRestore(index) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual version row in expanded timeline
 */
@Composable
fun VersionRow(
    version: VersionEntry,
    index: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRestore: () -> Unit
) {
    val colors = vibeColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ComponentSm, VibeSpacing.ComponentXs)
            .background(if (isCurrent) colors.accentContainer else colors.surfaceElevated, VibeShapes.Small)
            .border(VibeBorders.Hairline, if (isCurrent) colors.accent.copy(alpha = 0.3f) else colors.border, VibeShapes.Small)
            .clickable(onClick = onClick)
    ) {
        // Version indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (isCurrent) colors.accent
                    else if (version.success) colors.success
                    else colors.error,
                    VibeShapes.Circle
                )
                .padding(VibeSpacing.Sm),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                Icon(Icons.Filled.CheckCircle, "Current", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        // Version info
        Column(modifier = Modifier.weight(1f).padding(VibeSpacing.Sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = version.summary,
                    style = VibeTypography.BodyMedium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = version.timestamp,
                    style = VibeTypography.LabelSmall,
                    color = colors.textMuted
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)
            ) {
                version.filesChanged?.let { count ->
                    TagChip(text = "$count files", color = colors.accentContainer, textColor = colors.accent)
                }
                version.duration?.let { dur ->
                    TagChip(text = formatDuration(dur), color = colors.border, textColor = colors.textMuted)
                }
                if (!isCurrent) {
                    androidx.compose.material3.TextButton(onClick = onRestore, modifier = Modifier.padding(start = VibeSpacing.Sm)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs)) {
                            Icon(Icons.Filled.Restore, "Restore", tint = colors.accent, modifier = Modifier.size(14.dp))
                            Text("Restore", style = VibeTypography.LabelSmall, color = colors.accent)
                        }
                    }
                }
            }
        }

        // Status icon
        Icon(
            imageVector = if (version.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = if (version.success) "Success" else "Failed",
            tint = if (version.success) colors.success else colors.error,
            modifier = Modifier.padding(end = VibeSpacing.Sm).size(20.dp)
        )
    }
}

/**
 * Data class for version entry
 */
data class VersionEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val summary: String,
    val timestamp: String,
    val success: Boolean = true,
    val filesChanged: Int? = null,
    val duration: Long? = null,
    val commitHash: String? = null,
    val author: String = "Agent"
)

fun formatDuration(ms: Long): String {
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> "${(ms / 1000).toString()}s"
        else -> "${(ms / 60000).toString()}m ${((ms % 60000) / 1000).toString()}s"
    }
}

/**
 * Compact version indicator for chat header
 */
@Composable
fun VersionIndicator(
    currentVersion: Int,
    totalVersions: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    Box(
        modifier = modifier
            .padding(VibeSpacing.ComponentXs, VibeSpacing.ComponentXxs)
            .background(colors.accentContainer, VibeShapes.Pill)
            .border(VibeBorders.Hairline, colors.accent.copy(alpha = 0.3f), VibeShapes.Pill)
            .clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.History, "Versions", tint = colors.accent, modifier = Modifier.size(14.dp))
            Text(text = "v$currentVersion/$totalVersions", style = VibeTypography.LabelSmall, color = colors.accent)
        }
    }
}

/**
 * Undo toast/snackbar
 */
@Composable
fun UndoSnackbar(
    visible: Boolean,
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = vibeColors()

    androidx.compose.material3.SnackbarHost(modifier = Modifier) { data ->
        androidx.compose.material3.Snackbar(
            action = { androidx.compose.material3.TextButton(onClick = onUndo) { Text("UNDO", style = VibeTypography.LabelLarge, color = colors.accent) } },
            dismissAction = { onDismiss() },
            modifier = Modifier.padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Md)
        ) {
            Row(modifier = Modifier.padding(VibeSpacing.ComponentMd)) {
                Icon(Icons.Filled.CheckCircle, "Done", tint = colors.success, modifier = Modifier.size(20.dp).padding(end = VibeSpacing.Sm))
                Text(text = message, style = VibeTypography.BodyMedium, color = colors.text, modifier = Modifier.weight(1f))
            }
        }
    }
}