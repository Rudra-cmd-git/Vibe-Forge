package com.vibecoder.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.tap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.BaseTextField

/**
 * PillInput - The main floating input at bottom of chat
 * Pill-shaped, with attach, mic, send, and model selector
 */
@Composable
fun PillInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onMic: () -> Unit,
    onModelSelect: () -> Unit,
    modelName: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    placeholder: String = "Describe what you want to build...",
    isRecording: Boolean = false,
    isProcessing: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = vibeColors()
    val textFieldColors = TextFieldDefaults.textFieldColors(
        containerColor = colors.surface,
        focusedContainerColor = colors.surfaceElevated,
        unfocusedContainerColor = colors.surface,
        borderColor = { colors.border },
        focusedBorderColor = colors.accent,
        focusedLabelTextColor = colors.accent,
        cursorColor = colors.accent,
        placeholderTextColor = colors.textMuted,
        textColor = colors.text,
        leadingIconColor = colors.textMuted,
        trailingIconColor = colors.textMuted,
        disabledContainerColor = colors.surface.copy(alpha = 0.5f),
        disabledTextColor = colors.textSubtle,
        disabledPlaceholderTextColor = colors.textSubtle,
        disabledBorderColor = colors.border.copy(alpha = 0.5f)
    )

    val borderWidth by animateDpAsState(
        targetValue = if (enabled) VibeBorders.Hairline else VibeBorders.Hairline,
        animationSpec = VibeMotion.SpringFast
    )

    val backgroundColor by animateColorAsState(
        targetValue = colors.surface,
        animationSpec = VibeMotion.SpringNormal
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Sm)
            .padding(bottom = VibeSpacing.Md)
    ) {
        // Model selector chip above input
        ModelSelectorChip(
            modelName = modelName,
            onClick = onModelSelect,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = VibeSpacing.Xs)
        )

        // Main input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attach button
            IconButton(
                onClick = onAttach,
                enabled = enabled && !isProcessing,
                modifier = Modifier.padding(start = VibeSpacing.Xs)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Close else Icons.Filled.AttachFile,
                    contentDescription = if (isRecording) "Cancel recording" else "Attach image",
                    tint = if (isRecording) colors.error else colors.textMuted
                )
            }

            // Mic button
            IconButton(
                onClick = onMic,
                enabled = enabled && !isProcessing,
                modifier = Modifier.padding(start = VibeSpacing.Xs)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.Mic,
                    contentDescription = if (isRecording) "Stop recording" else "Voice input",
                    tint = if (isRecording) colors.accent else colors.textMuted
                )
            }

            // Text field
            androidx.compose.material3.TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = VibeSpacing.Sm)
                    .focusRequester(focusRequester),
                singleLine = false,
                maxLines = 5,
                textStyle = VibeTypography.BodyMedium,
                placeholder = { Text(text = placeholder, style = VibeTypography.BodyMedium, color = colors.textMuted) },
                colors = textFieldColors,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                    onDone = { onSend() }
                ),
                shape = VibeShapes.Pill,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                enabled = enabled && !isProcessing,
                visualTransformation = VisualTransformation.None
            )

            // Send button
            val sendEnabled = enabled && value.trim().isNotBlank() && !isProcessing
            IconButton(
                onClick = onSend,
                enabled = sendEnabled,
                modifier = Modifier
                    .padding(end = VibeSpacing.Xs)
                    .size(40.dp)
            ) {
                if (isProcessing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.accent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = if (sendEnabled) colors.accent else colors.textSubtle
                    )
                }
            }
        }
    }
}

/**
 * Model selector chip - shows current model, opens selector on tap
 */
@Composable
fun ModelSelectorChip(
    modelName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = vibeColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = VibeSpacing.Sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VibeSpacing.ComponentSm, VibeSpacing.ComponentXs)
                .background(
                    color = colors.accentContainer,
                    shape = VibeShapes.Pill
                )
                .border(VibeBorders.Hairline, colors.accent.copy(alpha = 0.3f), VibeShapes.Pill),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = "Model",
                tint = colors.accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = modelName,
                style = VibeTypography.LabelMedium,
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = colors.accent.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Compact version of PillInput for smaller spaces
 */
@Composable
fun CompactPillInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    placeholder: String = "Message...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = vibeColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttach, enabled = enabled, modifier = Modifier.padding(start = VibeSpacing.Xs)) {
            Icon(Icons.Filled.AttachFile, "Attach", tint = colors.textMuted)
        }

        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).padding(horizontal = VibeSpacing.Sm),
            singleLine = true,
            textStyle = VibeTypography.BodyMedium,
            placeholder = { Text(placeholder, style = VibeTypography.BodyMedium, color = colors.textMuted) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surfaceElevated,
                borderColor = { colors.border },
                focusedBorderColor = colors.accent,
                textColor = colors.text,
                placeholderTextColor = colors.textMuted
            ),
            shape = VibeShapes.Pill,
            enabled = enabled
        )

        IconButton(onClick = onSend, enabled = enabled && value.trim().isNotBlank(), modifier = Modifier.padding(end = VibeSpacing.Xs)) {
            Icon(Icons.Filled.Send, "Send", tint = colors.accent)
        }
    }
}