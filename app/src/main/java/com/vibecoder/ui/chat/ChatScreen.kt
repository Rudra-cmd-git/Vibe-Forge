package com.vibecoder.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalContext
import com.vibecoder.design.components.*
import com.vibecoder.design.theme.VibeSpacing
import com.vibecoder.design.theme.VibeTypography
import com.vibecoder.design.theme.vibeColors
import com.vibecoder.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    projectId: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val colors = vibeColors()
    val lazyListState = rememberLazyListState()
    val showPreview by remember { mutableStateOf(false) }
    val showTimeline by remember { mutableStateOf(false) }
    val showAdvanced by remember { mutableStateOf(false) }
    val previewSheetState = remember { PreviewSheetStateHolder(PreviewSheetState.Half) }

    // Observing view model state
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val agentActions by viewModel.agentActions.collectAsStateWithLifecycle()
    val agentStatus by viewModel.agentStatus.collectAsStateWithLifecycle()
    val isAgentWorking by viewModel.isAgentWorking.collectAsStateWithLifecycle()
    val versions by viewModel.versions.collectAsStateWithLifecycle()
    val currentVersion by viewModel.currentVersion.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize().background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top bar
            ChatTopBar(
                projectName = viewModel.projectName.collectAsStateWithLifecycle().value ?: projectId,
                onBack = onBack,
                onPreviewToggle = { showPreview = !showPreview },
                onTimelineToggle = { showTimeline = !showTimeline },
                onAdvancedToggle = { showAdvanced = !showAdvanced },
                showPreview = showPreview,
                showTimeline = showTimeline,
                currentVersion = currentVersion,
                totalVersions = versions.size
            )

            // Chat messages
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 0.dp, 0.dp, 100.dp), // Space for input
                    reverseLayout = false
                ) {
                    items(messages) { message ->
                        ChatMessage(message = message)
                    }

                    // Agent actions (tool chips)
                    items(agentActions) { action ->
                        ToolChip(
                            action = action,
                            expanded = action.status == ToolStatus.Running || action.status == ToolStatus.Pending
                        )
                    }

                    // Agent status line while working
                    if (isAgentWorking) {
                        item {
                            AgentStatusLine(status = agentStatus ?: "Working…")
                        }
                    }

                    // Shimmer while loading
                    if (viewModel.isLoading) {
                        item { ToolChipShimmer() }
                    }
                }
            }

            // Timeline bar (compact at bottom)
            if (versions.isNotEmpty()) {
                TimelineBar(
                    versions = versions,
                    currentVersion = currentVersion,
                    onVersionClick = { viewModel.restoreVersion(it) },
                    onUndo = { viewModel.undo() },
                    onRestore = { viewModel.restoreVersion(it) },
                    modifier = Modifier.padding(VibeSpacing.ScreenHorizontal, VibeSpacing.Sm)
                )
            }

            // Input
            PillInput(
                value = viewModel.inputText,
                onValueChange = { viewModel.setInputText(it) },
                onSend = { viewModel.sendMessage() },
                onAttach = { viewModel.attachImage() },
                onMic = { viewModel.toggleVoiceInput() },
                onModelSelect = { viewModel.showModelSelector() },
                modelName = viewModel.currentModelName,
                enabled = !isAgentWorking,
                isProcessing = isAgentWorking,
                placeholder = "Describe what you want to build…"
            )
        }

        // Preview Sheet
        if (showPreview) {
            PreviewSheet(
                isVisible = true,
                sheetState = previewSheetState.state,
                onStateChange = { previewSheetState.setState(it) },
                onClose = { showPreview = false },
                onReload = { viewModel.reloadPreview() },
                onDeviceSizeChange = { viewModel.setPreviewDeviceSize(it) },
                onOpenInBrowser = { viewModel.openInBrowser() },
                onToggleConsole = { viewModel.toggleConsole() },
                currentDeviceSize = viewModel.previewDeviceSize,
                showConsole = viewModel.showConsole,
                consoleContent = { ConsoleOutput(logs = viewModel.consoleLogs) },
                previewContent = { WebPreview(url = viewModel.previewUrl, deviceSize = viewModel.previewDeviceSize) }
            )
        }
    }
}

@Composable
fun ChatTopBar(
    projectName: String,
    onBack: () -> Unit,
    onPreviewToggle: () -> Unit,
    onTimelineToggle: () -> Unit,
    onAdvancedToggle: () -> Unit,
    showPreview: Boolean,
    showTimeline: Boolean,
    currentVersion: Int,
    totalVersions: Int
) {
    val colors = vibeColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.surface)
            .border(VibeBorders.Hairline, colors.border, VibeShapes.TopBar),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VibeSpacing.ScreenHorizontal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back + Project name
            Row(
                horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = colors.text)
                }
                Text(projectName, style = VibeTypography.TitleMedium, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                VersionIndicator(currentVersion = currentVersion, totalVersions = totalVersions, onClick = onTimelineToggle)
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)) {
                IconButton(onClick = onPreviewToggle, contentDescription = if (showPreview) "Hide preview" else "Show preview") {
                    Icon(Icons.Filled.RemoveRedEye, "Preview", tint = if (showPreview) colors.accent else colors.textMuted)
                }
                IconButton(onClick = onTimelineToggle, contentDescription = "Version timeline") {
                    Icon(Icons.Filled.History, "Timeline", tint = if (showTimeline) colors.accent else colors.textMuted)
                }
                IconButton(onClick = onAdvancedToggle, contentDescription = "Advanced") {
                    Icon(Icons.Filled.MoreVert, "Advanced", tint = colors.textMuted)
                }
            }
        }
    }
}

@Composable
fun ChatMessage(message: ChatMessageData) {
    val colors = vibeColors()

    when (message.role) {
        ChatRole.User -> UserMessage(message = message)
        ChatRole.Assistant -> AssistantMessage(message = message)
        ChatRole.System -> SystemMessage(message = message)
    }
}

@Composable
fun UserMessage(message: ChatMessageData) {
    val colors = vibeColors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .padding(VibeSpacing.Sm)
                .background(colors.accentContainer, VibeShapes.ChatBubbleUser)
                .border(VibeBorders.Hairline, colors.accent.copy(alpha = 0.2f), VibeShapes.ChatBubbleUser)
                .widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(VibeSpacing.ComponentMd),
                horizontalAlignment = Alignment.End
            ) {
                Text(message.content, style = VibeTypography.BodyMedium, color = colors.text)
                Text(message.timestamp, style = VibeTypography.LabelSmall, color = colors.textSubtle, modifier = Modifier.padding(top = VibeSpacing.Xxs))
            }
        }
    }
}

@Composable
fun AssistantMessage(message: ChatMessageData) {
    val colors = vibeColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.Sm)
            .background(colors.surface, VibeShapes.ChatBubble)
            .border(VibeBorders.Hairline, colors.border, VibeShapes.ChatBubble)
    ) {
        Column(
            modifier = Modifier.padding(VibeSpacing.ComponentMd),
            horizontalAlignment = Alignment.Start
        ) {
            Text(message.content, style = VibeTypography.BodyMedium, color = colors.text)
            if (message.metadata != null) {
                Text(message.metadata!!, style = VibeTypography.LabelSmall, color = colors.textSubtle, modifier = Modifier.padding(top = VibeSpacing.Xxs))
            }
        }
    }
}

@Composable
fun SystemMessage(message: ChatMessageData) {
    val colors = vibeColors()

    Text(
        text = message.content,
        style = VibeTypography.BodySmall,
        color = colors.textMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(VibeSpacing.Md)
    )
}

data class ChatMessageData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: ChatRole,
    val content: String,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
    val metadata: String? = null
)

enum class ChatRole { User, Assistant, System }

/**
 * Preview composable
 */
@Composable
fun ChatScreenPreview() {
    com.vibecoder.design.VibeTheme {
        val viewModel = ChatViewModel(
            projectId = "preview",
            projectName = "E-commerce Website",
            repository = object : ChatRepository { },
            agent = object : Agent { }
        )
        ChatScreen(projectId = "preview", viewModel = viewModel, onBack = {})
    }
}