package com.vibecoder.ui.setup

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.derivedStateOf
import com.vibecoder.design.components.TagChip
import com.vibecoder.design.theme.VibeSpacing
import com.vibecoder.design.theme.VibeTypography
import com.vibecoder.design.theme.vibeColors

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val colors = vibeColors()
    val currentStep by remember { mutableStateOf(SetupStep.Provider) }
    val selectedProvider by remember { mutableStateOf<ProviderOption?>(null) }
    val selectedModel by remember { mutableStateOf<String?>(null) }
    val apiKey by remember { mutableStateOf("") }
    val isOnlineBuild by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(VibeSpacing.ScreenHorizontal, VibeSpacing.ScreenVertical),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        SetupProgressIndicator(currentStep = currentStep)

        // Step content
        when (currentStep) {
            SetupStep.Provider -> ProviderSelectionScreen(
                selectedProvider = selectedProvider,
                onProviderSelect = { provider ->
                    selectedProvider = provider
                    // Auto-advance after selection
                },
                onNext = { currentStep = SetupStep.Model }
            )
            SetupStep.Model -> ModelSelectionScreen(
                provider = selectedProvider!!,
                selectedModel = selectedModel,
                onModelSelect = { model -> selectedModel = model },
                onNext = { currentStep = SetupStep.ApiKey },
                onBack = { currentStep = SetupStep.Provider }
            )
            SetupStep.ApiKey -> ApiKeyScreen(
                provider = selectedProvider!!,
                model = selectedModel!!,
                apiKey = apiKey,
                onApiKeyChange = { apiKey = it },
                onNext = { currentStep = SetupStep.BuildType },
                onBack = { currentStep = SetupStep.Model }
            )
            SetupStep.BuildType -> BuildTypeScreen(
                isOnlineBuild = isOnlineBuild,
                onBuildTypeChange = { isOnlineBuild = it },
                onNext = { currentStep = SetupStep.Runtime },
                onBack = { currentStep = SetupStep.ApiKey }
            )
            SetupStep.Runtime -> RuntimeSetupScreen(
                isOnlineBuild = isOnlineBuild,
                onComplete = onSetupComplete,
                onBack = { currentStep = SetupStep.BuildType }
            )
        }
    }
}

enum class SetupStep {
    Provider, Model, ApiKey, BuildType, Runtime
}

@Composable
fun SetupProgressIndicator(currentStep: SetupStep) {
    val colors = vibeColors()
    val steps = SetupStep.values()
    val currentIndex = steps.indexOf(currentStep)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(VibeSpacing.Xs)
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Filled.CheckCircle, "Done", tint = colors.success, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = VibeTypography.LabelMedium,
                            color = if (isCurrent) colors.accent else colors.textSubtle
                        )
                    }
                }
                Text(
                    text = step.name,
                    style = VibeTypography.LabelSmall,
                    color = if (isCurrent || isCompleted) colors.text else colors.textSubtle,
                    textAlign = TextAlign.Center
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .align(Alignment.CenterVertically)
                        .background(if (index < currentIndex) colors.success else colors.border, VibeShapes.Pill)
                )
            }
        }
    }
}

@Composable
fun ProviderSelectionScreen(
    selectedProvider: ProviderOption?,
    onProviderSelect: (ProviderOption) -> Unit,
    onNext: () -> Unit
) {
    val colors = vibeColors()
    val providers = ProviderOption.values()

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
    ) {
        Text("Choose your AI provider", style = VibeTypography.HeadlineSmall, color = colors.text, textAlign = TextAlign.Center)
        Text("We'll fetch available models from your provider", style = VibeTypography.BodyMedium, color = colors.textMuted, textAlign = TextAlign.Center)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, VibeSpacing.Sm)
        ) {
            items(providers) { provider ->
                ProviderCard(
                    provider = provider,
                    selected = selectedProvider == provider,
                    onClick = { onProviderSelect(provider); onNext() }
                )
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: ProviderOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = vibeColors()

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal)
            .clickable(onClick = onClick),
        shape = VibeShapes.Large,
        border = androidx.compose.ui.graphics.drawscope.Stroke(
            width = if (selected) VibeBorders.Emphasis else VibeBorders.Hairline,
            color = if (selected) colors.accent else colors.border
        ),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) colors.accentContainer else colors.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(VibeSpacing.ComponentLg),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(colors.accentContainer, VibeShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(provider.icon, contentDescription = provider.name, tint = colors.accent, modifier = Modifier.size(28.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs)
            ) {
                Text(provider.name, style = VibeTypography.TitleMedium, color = colors.text)
                Text(provider.description, style = VibeTypography.BodySmall, color = colors.textMuted)
            }
            if (selected) {
                Icon(Icons.Filled.CheckCircle, "Selected", tint = colors.accent, modifier = Modifier.size(24.dp))
            }
        }
    }
}

enum class ProviderOption(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val requiresApiKey: Boolean = true
) {
    Anthropic("Anthropic", "Claude models - best for coding", Icons.Filled.Psychology),
    OpenRouter("OpenRouter", "Access 100+ models (GPT, Llama, etc.)", Icons.Filled.Cloud),
    DeepSeek("DeepSeek", "Fast, affordable coding models", Icons.Filled.Build),
    Custom("Custom Endpoint", "OpenAI-compatible API (local, proxy, etc.)", Icons.Filled.Key, requiresApiKey = true)
}

@Composable
fun ModelSelectionScreen(
    provider: ProviderOption,
    selectedModel: String?,
    onModelSelect: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colors = vibeColors()
    // In real app, fetch models from API
    val models = remember { provider.defaultModels }

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = colors.text) }
            Column {
                Text("Select Model", style = VibeTypography.HeadlineSmall, color = colors.text)
                Text("${provider.name} models", style = VibeTypography.BodySmall, color = colors.textMuted)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)
        ) {
            items(models) { model ->
                ModelCard(
                    model = model,
                    selected = selectedModel == model.id,
                    onClick = { onModelSelect(model.id); onNext() }
                )
            }
        }
    }
}

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val contextWindow: String,
    val pricing: String
)

@Composable
fun ModelCard(
    model: ModelInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = vibeColors()

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal)
            .clickable(onClick = onClick),
        shape = VibeShapes.Medium,
        border = androidx.compose.ui.graphics.drawscope.Stroke(
            width = if (selected) VibeBorders.Emphasis else VibeBorders.Hairline,
            color = if (selected) colors.accent else colors.border
        ),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) colors.accentContainer else colors.surface
        )
    ) {
        Column(modifier = Modifier.padding(VibeSpacing.ComponentMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(model.name, style = VibeTypography.TitleSmall, color = colors.text)
                    Text(model.description, style = VibeTypography.BodySmall, color = colors.textMuted)
                }
                if (selected) {
                    Icon(Icons.Filled.CheckCircle, "Selected", tint = colors.accent, modifier = Modifier.size(24.dp))
                }
            }
            Row(
                modifier = Modifier.padding(top = VibeSpacing.Sm).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
            ) {
                TagChip(text = model.contextWindow, color = colors.border, textColor = colors.textMuted)
                TagChip(text = model.pricing, color = colors.border, textColor = colors.textMuted)
            }
        }
    }
}

val ProviderOption.defaultModels: List<ModelInfo>
    get() = when (this) {
        ProviderOption.Anthropic -> listOf(
            ModelInfo("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet", "Best for complex coding tasks", "200k tokens", "\$3/\$15 per 1M tokens"),
            ModelInfo("claude-3-5-haiku-20241022", "Claude 3.5 Haiku", "Fast, affordable coding", "200k tokens", "\$1/\$5 per 1M tokens"),
            ModelInfo("claude-3-opus-20240229", "Claude 3 Opus", "Most capable model", "200k tokens", "\$15/\$75 per 1M tokens")
        )
        ProviderOption.OpenRouter -> listOf(
            ModelInfo("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet (via OpenRouter)", "Best for complex coding", "200k tokens", "Pay per use"),
            ModelInfo("openai/gpt-4o", "GPT-4o (via OpenRouter)", "Fast multimodal model", "128k tokens", "Pay per use"),
            ModelInfo("meta-llama/llama-3.1-405b", "Llama 3.1 405B (via OpenRouter)", "Largest open model", "128k tokens", "Pay per use")
        )
        ProviderOption.DeepSeek -> listOf(
            ModelInfo("deepseek-coder", "DeepSeek Coder", "Specialized for code", "128k tokens", "\$0.14/\$0.28 per 1M tokens"),
            ModelInfo("deepseek-chat", "DeepSeek Chat", "General purpose", "128k tokens", "\$0.14/\$0.28 per 1M tokens")
        )
        ProviderOption.Custom -> listOf(
            ModelInfo("custom", "Custom Model", "Enter model ID manually", "Depends on endpoint", "Your pricing")
        )
    }

@Composable
fun ApiKeyScreen(
    provider: ProviderOption,
    model: String,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colors = vibeColors()

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = colors.text) }
            Column {
                Text("API Key", style = VibeTypography.HeadlineSmall, color = colors.text)
                Text("${provider.name} • $model", style = VibeTypography.BodySmall, color = colors.textMuted)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md)) {
            Text("Paste your API key", style = VibeTypography.TitleMedium, color = colors.text)
            Text("Your key is stored securely in Android Keystore and never leaves your device", style = VibeTypography.BodySmall, color = colors.textMuted)

            androidx.compose.material3.TextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = VibeTypography.BodyMedium,
                placeholder = { Text("sk-... or your API key", style = VibeTypography.BodyMedium, color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Filled.Key, "API Key", tint = colors.textMuted) },
                trailingIcon = {
                    if (apiKey.isNotBlank()) {
                        Icon(Icons.Filled.Verified, "Valid format", tint = colors.success, modifier = Modifier.padding(end = VibeSpacing.ComponentMd).size(24.dp))
                    }
                },
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                    containerColor = colors.surface,
                    focusedContainerColor = colors.surfaceElevated,
                    unfocusedContainerColor = colors.surface,
                    borderColor = { colors.border },
                    focusedBorderColor = colors.accent,
                    textColor = colors.text,
                    placeholderTextColor = colors.textMuted
                ),
                shape = VibeShapes.Pill,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text)
            )

            androidx.compose.material3.FilledButton(
                onClick = { if (apiKey.isNotBlank()) onNext() },
                enabled = apiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = VibeSpacing.Md),
                shape = VibeShapes.Pill,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    disabledContainerColor = colors.border
                )
            ) {
                Text("Continue", style = VibeTypography.LabelLarge)
            }
        }
    }
}

@Composable
fun BuildTypeScreen(
    isOnlineBuild: Boolean,
    onBuildTypeChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colors = vibeColors()

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = colors.text) }
            Column {
                Text("Choose build type", style = VibeTypography.HeadlineSmall, color = colors.text)
                Text("Both include the full runtime", style = VibeTypography.BodySmall, color = colors.textMuted)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md)
        ) {
            BuildTypeCard(
                title = "Online APK",
                description = "Small download (~15 MB). Runtime bundles downloaded on first run with SHA-256 verification.",
                icon = Icons.Filled.Cloud,
                size = "~15 MB",
                selected = isOnlineBuild,
                onClick = { onBuildTypeChange(true); onNext() }
            )

            BuildTypeCard(
                title = "Offline APK",
                description = "Everything bundled (~200 MB). Works completely offline, no downloads needed.",
                icon = Icons.Filled.Download,
                size = "~200 MB",
                selected = !isOnlineBuild,
                onClick = { onBuildTypeChange(false); onNext() }
            )
        }
    }
}

@Composable
fun BuildTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = vibeColors()

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VibeSpacing.ScreenHorizontal)
            .clickable(onClick = onClick),
        shape = VibeShapes.Large,
        border = androidx.compose.ui.graphics.drawscope.Stroke(
            width = if (selected) VibeBorders.Emphasis else VibeBorders.Hairline,
            color = if (selected) colors.accent else colors.border
        ),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) colors.accentContainer else colors.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(VibeSpacing.ComponentLg),
            horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(if (selected) colors.accent else colors.accentContainer, VibeShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = if (selected) Color.White else colors.accent, modifier = Modifier.size(28.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VibeSpacing.Xxs)
            ) {
                Text(title, style = VibeTypography.TitleMedium, color = colors.text)
                Text(description, style = VibeTypography.BodySmall, color = colors.textMuted)
                TagChip(text = size, color = if (selected) colors.accent else colors.border, textColor = if (selected) colors.accent else colors.textMuted)
            }
            if (selected) {
                Icon(Icons.Filled.CheckCircle, "Selected", tint = colors.accent, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun RuntimeSetupScreen(
    isOnlineBuild: Boolean,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val colors = vibeColors()
    val steps = remember {
        listOf(
            SetupProgressStep("Downloading runtime bundles", false),
            SetupProgressStep("Verifying SHA-256 checksums", false),
            SetupProgressStep("Installing Node, npm, Python, Git", false),
            SetupProgressStep("Setting up web stack (Vite, React, Tailwind)", false),
            SetupProgressStep("Configuring PRoot environment", false),
            SetupProgressStep("Ready to code!", false)
        )
    }
    val currentStepIndex = remember { mutableStateOf(0) }
    val isComplete = remember { mutableStateOf(false) }

    // Simulate progress
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!isComplete.value) {
            steps.indices.forEach { index ->
                currentStepIndex.value = index
                steps[index] = steps[index].copy(completed = true)
                try { Thread.sleep(1500) } catch (e: InterruptedException) {}
            }
            isComplete.value = true
            onComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isComplete.value) {
            Icon(Icons.Filled.Build, "Setting up", tint = colors.accent, modifier = Modifier.size(64.dp))
            Text("Setting up your runtime", style = VibeTypography.HeadlineSmall, color = colors.text)
            Text("This happens once. Grab a coffee ☕", style = VibeTypography.BodyMedium, color = colors.textMuted)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(VibeSpacing.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(VibeSpacing.Sm)
            ) {
                items(steps) { step ->
                    SetupProgressRow(step = step)
                }
            }
        } else {
            Icon(Icons.Filled.CheckCircle, "Complete", tint = colors.success, modifier = Modifier.size(64.dp))
            Text("All set!", style = VibeTypography.HeadlineSmall, color = colors.text)
            Text("Your coding environment is ready", style = VibeTypography.BodyMedium, color = colors.textMuted)

            androidx.compose.material3.FilledButton(
                onClick = onComplete,
                modifier = Modifier.padding(top = VibeSpacing.Xl).width(200.dp),
                shape = VibeShapes.Pill,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White)
            ) {
                Text("Start Building", style = VibeTypography.LabelLarge)
            }
        }
    }
}

data class SetupProgressStep(val label: String, val completed: Boolean)

@Composable
fun SetupProgressRow(step: SetupProgressStep) {
    val colors = vibeColors()

    Row(
        modifier = Modifier.fillMaxWidth().padding(VibeSpacing.ComponentSm),
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (step.completed) {
                Icon(Icons.Filled.CheckCircle, "Done", tint = colors.success, modifier = Modifier.size(16.dp))
            } else {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.accent,
                    strokeWidth = 2.dp
                )
            }
        }
        Text(step.label, style = VibeTypography.BodyMedium, color = if (step.completed) colors.text else colors.textMuted)
    }
}