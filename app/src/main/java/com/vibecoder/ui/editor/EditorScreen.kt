package com.vibecoder.ui.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Copy
import androidx.compose.material.icons.filled.GitHub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.ComposableViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun EditorScreen(projectId: String, onNavigate: (String) -> Unit) {
    val viewModel: EditorViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    var promptText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { 
                    Text(if (projectId == "new") "New Project" : "Project Editor", maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (projectId != "new") {
                        IconButton(onClick = { viewModel.pushToGitHub() }, enabled = !isGenerating) {
                            Icon(Icons.Default.GitHub, contentDescription = "Push to GitHub")
                        }
                        IconButton(onClick = { viewModel.commitChanges() }, enabled = !isGenerating) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Commit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            
            // Main content area
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Prompt input area
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("What do you want to build?", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                modifier = Modifier.fillMaxSize(),
                                label = { Text("Describe your project...") },
                                placeholder = { Text("e.g., A todo app with React and TypeScript, using localStorage for persistence") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.foundation.text.KeyboardType.Text,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                maxLines = 6,
                                singleLine = false
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isGenerating) {
                                    Button(onClick = { viewModel.stopGeneration() }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Stop, contentDescription = "")
                                            spacer(Modifier.width(8.dp))
                                            Text("Stop")
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { 
                                            if (promptText.isNotBlank()) {
                                                isGenerating = true
                                                scope.launch { viewModel.generateProject(promptText) }
                                            }
                                        },
                                        enabled = promptText.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().width(200.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "")
                                            spacer(Modifier.width(8.dp))
                                            Text("Generate")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Progress / Status area
                    when (state) {
                        is EditorState.Generating -> {
                            GeneratingView(
                                progress = state.progress,
                                currentStep = state.currentStep,
                                logs = state.logs
                            )
                        }
                        is EditorState.Success -> {
                            SuccessView(
                                message = state.message,
                                files = state.files,
                                onOpenEditor = { viewModel.openFileEditor(state.projectPath) }
                            )
                        }
                        is EditorState.Error -> {
                            ErrorView(message = state.message, onRetry = { isGenerating = false })
                        }
                        else -> {
                            // Idle state - show project info if existing project
                            if (projectId != "new") {
                                ProjectInfoCard(projectId = projectId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingView(progress: Float, currentStep: String, logs: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Building your project...", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text("${(progress * 100).toInt()}%")
                }
                LinearProgressIndicator(progress = progress)
                Text(currentStep, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Logs
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Build Log", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    }
                    
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            reverseLayout = true,
                            autoScroll = true
                        ) {
                            items(logs.reversed()) { log ->
                                Text(
                                    log,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessView(message: String, files: List<String>, onOpenEditor: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.successContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer, modifier = Modifier.size(64.dp))
            Text("Project Ready!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer)
            Text(message, textAlign = TextAlign.Center, color = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer.copy(alpha = 0.8f))
            
            if (files.isNotEmpty()) {
                Text("${files.size} files created", fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer.copy(alpha = 0.7f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    files.take(10).forEach { file ->
                        Text(file, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer.copy(alpha = 0.6f))
                    }
                    if (files.size > 10) {
                        Text("... and ${files.size - 10} more", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSuccessContainer.copy(alpha = 0.5f))
                    }
                }
            }
            
            Button(onClick = onOpenEditor, modifier = Modifier.fillMaxWidth().width(240.dp)) {
                Text("Open in Editor")
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Close, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(64.dp))
            Text("Generation Failed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer)
            Text(message, textAlign = TextAlign.Center, color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().width(200.dp)) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun ProjectInfoCard(projectId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Project: $projectId", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text("Use the prompt above to add features or make changes to your project.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { /* open file tree */ }) { Text("Browse Files") }
                spacer(Modifier.width(8.dp))
                Button(onClick = { /* open terminal */ }) { Text("Terminal") }
            }
        }
    }
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val agentExecutor: com.vibecoder.agent.AgentExecutor,
    private val gitHubRepository: com.vibecoder.data.github.GitHubRepository,
    private val database: com.vibecoder.data.storage.Database
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data class Generating(
            val progress: Float,
            val currentStep: String,
            val logs: List<String>
        ) : UiState
        data class Success(
            val message: String,
            val files: List<String>,
            val projectPath: String
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState

    private var currentTask: com.vibecoder.agent.AgentTask? = null

    fun generateProject(prompt: String) {
        _uiState.value = UiState.Generating(0f, "Initializing...", emptyList())
        
        viewModelScope.launch {
            // This would use the AgentExecutor to run the generation
            // For now, simulate progress
            val steps = listOf(
                "Analyzing request...",
                "Creating project structure...",
                "Writing source files...",
                "Setting up dependencies...",
                "Generating configuration...",
                "Running initial build...",
                "Finalizing..."
            )
            
            var logs = mutableListOf<String>()
            for ((index, step) in steps.withIndex()) {
                logs += "[${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}] $step"
                _uiState.value = UiState.Generating(
                    progress = (index + 1).toFloat() / steps.size,
                    currentStep = step,
                    logs = logs
                )
                delay(1000)
            }
            
            // Simulate success
            _uiState.value = UiState.Success(
                message = "Your project has been created successfully!",
                files = listOf("src/main.kt", "build.gradle.kts", "README.md", ".gitignore", "src/ui/MainScreen.kt"),
                projectPath = "/data/user/0/com.vibecoder/files/projects/$projectId"
            )
        }
    }

    fun stopGeneration() {
        // Cancel the current task
    }

    fun commitChanges() {
        // Commit local changes
    }

    fun pushToGitHub() {
        // Push to GitHub with secret scanning
    }

    fun openFileEditor(projectPath: String) {
        // Navigate to file editor
    }
}