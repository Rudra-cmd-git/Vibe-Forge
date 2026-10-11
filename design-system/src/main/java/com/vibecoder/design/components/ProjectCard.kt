package com.vibecoder.design.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.blendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ClockLoader
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.derivedStateOf

/**
 * ProjectCard - Template cards and recent project cards
 * Used on Home screen for templates and recent projects with live thumbnails
 */
@Composable
fun ProjectCard(
    project: ProjectCardData,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    val isTemplate = project.isTemplate

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = null
            ),
        shape = VibeShapes.Large,
        border = BorderStroke(VibeBorders.Hairline, colors.border),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = colors.surface,
            // contentColor = colors.text
        )
    ) {
        Column(
            modifier = Modifier.padding(VibeSpacing.ComponentMd)
        ) {
            // Thumbnail or icon area
            if (project.thumbnail != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(VibeShapes.Medium)
                        .background(colors.surfaceElevated)
                ) {
                    project.thumbnail?.let { painter ->
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = project.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(VibeShapes.Medium)
                        )
                    }
                }
            } else if (isTemplate) {
                // Template icon with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(VibeShapes.Medium)
                        .background(
                            color = colors.accentContainer,
                            shape = VibeShapes.Medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = project.icon,
                        contentDescription = project.name,
                        tint = colors.accent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Recent project - folder icon or first file type
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(VibeShapes.Medium)
                        .background(colors.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = project.icon,
                        contentDescription = project.name,
                        tint = colors.textMuted,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VibeSpacing.Sm),
                horizontalAlignment = Alignment.Start
            ) {
                // Header row: name + menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        style = VibeTypography.TitleMedium,
                        color = colors.text,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = VibeSpacing.Sm)
                    )
                    onMenuClick?.let { menuClick ->
                        androidx.compose.material3.IconButton(onClick = menuClick) {
                            Icon(Icons.Filled.MoreVert, "Menu", tint = colors.textMuted)
                        }
                    }
                }

                // Description
                project.description?.let { desc ->
                    Text(
                        text = desc,
                        style = VibeTypography.BodySmall,
                        color = colors.textMuted,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VibeSpacing.Xxs)
                    )
                }

                // Tags / tech stack
                if (project.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VibeSpacing.Sm),
                        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)
                    ) {
                        project.tags.take(4).forEach { tag ->
                            TagChip(text = tag, color = colors.accentContainer, textColor = colors.accent)
                        }
                        if (project.tags.size > 4) {
                            TagChip(text = "+${project.tags.size - 4}", color = colors.border, textColor = colors.textMuted)
                        }
                    }
                }

                // Footer: status, last modified, etc.
                if (!isTemplate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VibeSpacing.Sm),
                        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(6.dp)
                                    .background(project.status.color, VibeShapes.Circle)
                            )
                            Text(
                                text = project.status.label,
                                style = VibeTypography.LabelSmall,
                                color = project.status.color
                            )
                        }

                        // Last modified
                        Text(
                            text = project.lastModified,
                            style = VibeTypography.LabelSmall,
                            color = colors.textSubtle
                        )

                        // Size
                        project.size?.let { size ->
                            Text(
                                text = size,
                                style = VibeTypography.LabelSmall,
                                color = colors.textSubtle
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Template card variant - larger, more visual
 */
@Composable
fun TemplateCard(
    template: TemplateData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(VibeSpacing.ScreenHorizontal),
        shape = VibeShapes.ExtraLarge,
        border = BorderStroke(VibeBorders.Hairline, colors.border),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(VibeSpacing.ComponentLg)
        ) {
            // Icon with accent background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.accentContainer, VibeShapes.Large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = template.icon,
                    contentDescription = template.name,
                    tint = colors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Name
            Text(
                text = template.name,
                style = VibeTypography.HeadlineSmall,
                color = colors.text,
                modifier = Modifier.padding(top = VibeSpacing.Md)
            )

            // Description
            Text(
                text = template.description,
                style = VibeTypography.BodyMedium,
                color = colors.textMuted,
                modifier = Modifier.padding(top = VibeSpacing.Xs)
            )

            // Tags
            Row(
                modifier = Modifier.padding(top = VibeSpacing.Sm),
                horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)
            ) {
                template.tags.forEach { tag ->
                    TagChip(text = tag, color = colors.accentContainer, textColor = colors.accent)
                }
            }

            // Start button
            androidx.compose.material3.FilledButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VibeSpacing.Lg),
                shape = VibeShapes.Pill,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Start Building", style = VibeTypography.LabelLarge)
            }
        }
    }
}

/**
 * Simple tag chip
 */
@Composable
fun TagChip(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .padding(VibeSpacing.ComponentXs, VibeSpacing.ComponentXxs)
            .background(color, VibeShapes.Pill)
            .clickable(onClick = onClick, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() })
    ) {
        Text(text = text, style = VibeTypography.LabelSmall, color = textColor)
    }
}

/**
 * Data classes
 */
data class ProjectCardData(
    val id: String,
    val name: String,
    val description: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val thumbnail: androidx.compose.ui.graphics.painter.Painter? = null,
    val tags: List<String> = emptyList(),
    val status: ProjectStatus = ProjectStatus.Active,
    val lastModified: String = "Just now",
    val size: String? = null,
    val isTemplate: Boolean = false
)

data class TemplateData(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tags: List<String> = emptyList(),
    val prompt: String // The prompt to use when starting from this template
)

enum class ProjectStatus(val label: String, val color: Color) {
    Active("Active", Color(0xFF4ADE9A)),
    Building("Building...", Color(0xFFFFD166)),
    Error("Error", Color(0xFFFF6B6B)),
    Completed("Ready", Color(0xFF8B7CFF)),
    Archived("Archived", Color(0xFF8A8A99))
}

/**
 * Recent project item for list view
 */
@Composable
fun RecentProjectItem(
    project: ProjectCardData,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Sm)
            .clickable(onClick = onClick)
            .background(colors.surface, VibeShapes.Medium),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(VibeShapes.Small)
                .background(colors.surfaceElevated)
        ) {
            project.thumbnail?.let { painter ->
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = project.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(VibeShapes.Small)
                )
            } ?: Icon(
                imageVector = project.icon,
                contentDescription = project.name,
                tint = colors.textMuted,
                modifier = Modifier.size(28.dp).align(Alignment.Center)
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = project.name, style = VibeTypography.TitleSmall, color = colors.text, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(text = project.lastModified, style = VibeTypography.LabelSmall, color = colors.textSubtle)
            }
            project.description?.let { desc ->
                Text(text = desc, style = VibeTypography.BodySmall, color = colors.textMuted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = VibeSpacing.Xxs).fillMaxWidth())
            }
            Row(modifier = Modifier.padding(top = VibeSpacing.Xxs), horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)) {
                project.tags.take(3).forEach { tag ->
                    TagChip(text = tag, color = colors.accentContainer, textColor = colors.accent)
                }
            }
        }

        // Menu
        androidx.compose.material3.IconButton(onClick = onMenuClick) {
            Icon(Icons.Filled.MoreVert, "Menu", tint = colors.textMuted)
        }
    }
}

/**
 * Empty state for no projects
 */
@Composable
fun EmptyProjectsState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Xxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "No projects",
                tint = colors.textSubtle,
                modifier = Modifier.size(64.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No projects yet", style = VibeTypography.HeadlineSmall, color = colors.text)
                Text(text = "Start building your first project", style = VibeTypography.BodyMedium, color = colors.textMuted, modifier = Modifier.padding(top = VibeSpacing.Xs))
            }
            androidx.compose.material3.FilledButton(
                onClick = onCreateClick,
                shape = VibeShapes.Pill,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White)
            ) {
                Text(text = "Create Project", style = VibeTypography.LabelLarge)
            }
        }
    }
}