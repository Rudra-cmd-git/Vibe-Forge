package com.vibecoder.design.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * ToolChip - Quiet one-line chips showing agent actions
 * "Edited 3 files", "Ran build", "Fixed 2 errors"
 * Expand on tap to show details
 */
@Composable
fun ToolChip(
    action: ToolAction,
    onClick: (ToolAction) -> Unit = {},
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {}
) {
    val colors = vibeColors()
    val isExpanded = remember { mutableStateOf(expanded) }
    val currentExpanded = expanded || isExpanded.value

    val backgroundColor by animateDpAsState(
        targetValue = if (currentExpanded) 1.dp else 0.dp,
        animationSpec = Spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val detailsHeight = remember { mutableStateOf(0f) }

    val icon = when (action.type) {
        ToolActionType.Bash -> Icons.Filled.Terminal
        ToolActionType.ReadFile, ToolActionType.WriteFile, ToolActionType.EditFile -> Icons.Filled.FileCopy
        ToolActionType.ListDir -> Icons.Filled.Folder
        ToolActionType.SearchCode -> Icons.Filled.Code
        ToolActionType.WebFetch -> Icons.Filled.Web
        ToolActionType.GitCommit -> Icons.Filled.Cloud
        ToolActionType.GitCheckout -> Icons.Filled.Download
        ToolActionType.Build -> Icons.Filled.Build
        ToolActionType.Test -> Icons.Filled.BugReport
        ToolActionType.Plan -> Icons.Filled.Edit
        ToolActionType.Apply -> Icons.Filed.CheckCircle
        ToolActionType.Error -> Icons.Filled.Error
        ToolActionType.Success -> Icons.Filled.CheckCircle
        ToolActionType.Thinking -> Icons.Filled.HourglassTop
        ToolActionType.Other -> Icons.Filed.Storage
    }

    val iconColor = when (action.type) {
        ToolActionType.Error -> colors.error
        ToolActionType.Success, ToolActionType.GitCommit, ToolActionType.Apply -> colors.success
        ToolActionType.Build, ToolActionType.Test, ToolActionType.Plan -> colors.accent
        ToolActionType.Thinking -> colors.warning
        else -> colors.textMuted
    }

    val statusText = when (action.status) {
        ToolStatus.Pending -> "Pending..."
        ToolStatus.Running -> "Running..."
        ToolStatus.Completed -> "Done"
        ToolStatus.Failed -> "Failed"
        ToolStatus.Cancelled -> "Cancelled"
    }

    Column(modifier = modifier.animateContentSize()) {
        // Main chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VibeSpacing.ComponentSm, VibeSpacing.ComponentXs)
                .background(
                    color = if (currentExpanded) colors.surfaceElevated else colors.surface,
                    shape = VibeShapes.ToolChip
                )
                .border(VibeBorders.Hairline, colors.border, VibeShapes.ToolChip)
                .clickable { onClick(action); isExpanded.value = !isExpanded.value },
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier.size(8.dp)
                    .background(iconColor, VibeShapes.Circle)
            )

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = action.type.name,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )

            // Action summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.summary,
                    style = VibeTypography.BodyMedium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (action.detail != null && !currentExpanded) {
                    Text(
                        text = action.detail!!,
                        style = VibeTypography.BodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    style = VibeTypography.LabelSmall,
                    color = when (action.status) {
                        ToolStatus.Completed -> colors.success
                        ToolStatus.Failed -> colors.error
                        ToolStatus.Running, ToolStatus.Pending -> colors.warning
                        else -> colors.textMuted
                    }
                )
                if (action.duration != null) {
                    Text(
                        text = formatDuration(action.duration!!),
                        style = VibeTypography.LabelSmall,
                        color = colors.textSubtle
                    )
                }
            }

            // Expand indicator
            Icon(
                imageVector = if (currentExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (currentExpanded) "Collapse" else "Expand",
                tint = colors.textSubtle,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expanded details
        if (currentExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(VibeSpacing.ComponentSm, VibeSpacing.ComponentXs)
                    .background(colors.surfaceElevated, VibeShapes.ToolChip)
                    .border(VibeBorders.Hairline, colors.border.copy(alpha = 0.5f), VibeShapes.ToolChip)
                    .animateContentSize()
            ) {
                if (action.details.isNotEmpty()) {
                    Text(
                        text = "Details",
                        style = VibeTypography.LabelMedium,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = VibeSpacing.Xs)
                    )
                    action.details.forEach { detail ->
                        ToolDetailRow(
                            label = detail.key,
                            value = detail.value,
                            color = colors.text
                        )
                    }
                }

                if (action.files.isNotEmpty()) {
                    Text(
                        text = "Files (${action.files.size})",
                        style = VibeTypography.LabelMedium,
                        color = colors.textMuted,
                        modifier = Modifier.padding(top = VibeSpacing.Sm, bottom = VibeSpacing.Xs)
                    )
                    action.files.forEach { file ->
                        ToolFileRow(
                            path = file.path,
                            action = file.action,
                            onClick = { onClick(action.copy(details = action.details + ("File" to file.path))) }
                        )
                    }
                }

                if (action.output != null && action.output!!.isNotBlank()) {
                    Text(
                        text = "Output",
                        style = VibeTypography.LabelMedium,
                        color = colors.textMuted,
                        modifier = Modifier.padding(top = VibeSpacing.Sm, bottom = VibeSpacing.Xs)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(VibeSpacing.ComponentSm)
                            .background(colors.background, VibeShapes.CodeBlock)
                            .border(VibeBorders.Hairline, colors.border, VibeShapes.CodeBlock)
                    ) {
                        Text(
                            text = action.output!!,
                            style = codeStyle(CodeSize.Small),
                            color = colors.text,
                            maxLines = 20,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(VibeSpacing.ComponentSm).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Row for key-value detail in expanded tool chip
 */
@Composable
fun ToolDetailRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ComponentXs, 0),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)
    ) {
        Text(text = "$label:", style = VibeTypography.BodySmall, color = colors.textMuted)
        Text(
            text = value,
            style = VibeTypography.BodySmall,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Row for file in expanded tool chip
 */
@Composable
fun ToolFileRow(
    path: String,
    action: FileAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    val (icon, actionColor) = when (action) {
        FileAction.Created -> Icons.Filed.FileCopy to colors.success
        FileAction.Modified -> Icons.Filed.Edit to colors.accent
        FileAction.Deleted -> Icons.Filed.Delete to colors.error
        FileAction.Copied -> Icons.Filed.ContentCopy to colors.warning
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ComponentXs, 0)
            .clickable(onClick = onClick)
            .padding(VibeSpacing.ComponentSm)
            .background(colors.surface, VibeShapes.FileTreeItem),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = action.name, tint = actionColor, modifier = Modifier.size(16.dp))
        Text(text = path, style = VibeTypography.BodySmall, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(text = action.name, style = VibeTypography.LabelSmall, color = actionColor)
    }
}

enum class FileAction(val name: String) {
    Created("Created"),
    Modified("Modified"),
    Deleted("Deleted"),
    Copied("Copied")
}

/**
 * Shimmer loading chip for "thinking" state
 */
@Composable
fun ToolChipShimmer(modifier: Modifier = Modifier) {
    val colors = vibeColors()
    val shimmerAlpha = remember { mutableStateOf(0.3f) }
    // Animate shimmer
    androidx.compose.animation.animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, delayMillis = 0, easing = androidx.compose.animation.core.Easing.InOutCubic),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    ).value.also { shimmerAlpha.value = it }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ComponentSm, VibeSpacing.ComponentXs)
            .background(colors.surface, VibeShapes.ToolChip)
            .border(VibeBorders.Hairline, colors.border, VibeShapes.ToolChip),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(colors.accent.copy(alpha = shimmerAlpha.value), VibeShapes.Circle))
        Box(modifier = Modifier.width(18.dp).height(18.dp).background(colors.border.copy(alpha = shimmerAlpha.value), VibeShapes.Circle))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.height(16.dp).fillMaxWidth().background(colors.border.copy(alpha = shimmerAlpha.value), VibeShapes.ExtraSmall))
            Box(modifier = Modifier.height(12.dp).width(60.dp).background(colors.border.copy(alpha = shimmerAlpha.value), VibeShapes.ExtraSmall).padding(top = VibeSpacing.Xxs))
        }
        Box(modifier = Modifier.width(60.dp).height(16.dp).background(colors.border.copy(alpha = shimmerAlpha.value), VibeShapes.ExtraSmall))
        Box(modifier = Modifier.size(20.dp))
    }
}

/**
 * Status line shown while agent is working
 * "Planning...", "Building...", "Testing..."
 */
@Composable
fun AgentStatusLine(
    status: String,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Soft shimmer indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(colors.accent, VibeShapes.Circle)
                .graphicsLayer { alpha = 0.6f + 0.4f * android.view.animation.AnimationUtils.currentAnimationTimeMillis() % 2000 / 2000f }
        )
        Text(text = status, style = VibeTypography.BodyMedium, color = colors.textMuted)
        progress?.let { p ->
            androidx.compose.material3.LinearProgressIndicator(
                progress = p,
                modifier = Modifier.fillMaxWidth().padding(start = VibeSpacing.Lg).height(2.dp),
                color = colors.accent,
                trackColor = colors.border
            )
        }
    }
}

/**
 * Data classes for tool actions
 */
data class ToolAction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ToolActionType,
    val summary: String,
    val detail: String? = null,
    val details: List<Pair<String, String>> = emptyList(),
    val files: List<ToolFile> = emptyList(),
    val output: String? = null,
    val status: ToolStatus = ToolStatus.Pending,
    val duration: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun copy(
        id: String = this.id,
        type: ToolActionType = this.type,
        summary: String = this.summary,
        detail: String? = this.detail,
        details: List<Pair<String, String>> = this.details,
        files: List<ToolFile> = this.files,
        output: String? = this.output,
        status: ToolStatus = this.status,
        duration: Long? = this.duration,
        timestamp: Long = this.timestamp
    ) = ToolAction(id, type, summary, detail, details, files, output, status, duration, timestamp)
}

data class ToolFile(
    val path: String,
    val action: FileAction
)

enum class ToolActionType {
    Bash, ReadFile, WriteFile, EditFile, ListDir, SearchCode, WebFetch,
    GitCommit, GitCheckout, Build, Test, Plan, Apply, Error, Success, Thinking, Other
}

enum class ToolStatus {
    Pending, Running, Completed, Failed, Cancelled
}

fun formatDuration(ms: Long): String {
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> "${(ms / 1000).toString()}s"
        else -> "${(ms / 60000).toString()}m ${((ms % 60000) / 1000).toString()}s"
    }
}