package com.vibecoder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibecoder.agent.Agent
import com.vibecoder.storage.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val projectId: String,
    val projectName: String,
    private val repository: ChatRepository,
    private val agent: Agent
) : ViewModel() {

    // Input state
    private val _inputText = MutableStateFlow("")
    val inputText: String = ""
        @Suppress("UNUSED_GETTER")
        get() = _inputText.value
    fun setInputText(text: String) { _inputText.value = text }

    // Messages
    private val _messages = MutableStateFlow<List<ChatMessageData>>(emptyList())
    val messages = _messages.asStateFlow()

    // Agent actions
    private val _agentActions = MutableStateFlow<List<ToolAction>>(emptyList())
    val agentActions = _agentActions.asStateFlow()

    // Agent status
    private val _agentStatus = MutableStateFlow<String?>(null)
    val agentStatus = _agentStatus.asStateFlow()
    private val _isAgentWorking = MutableStateFlow(false)
    val isAgentWorking = _isAgentWorking.asStateFlow()

    // Versions
    private val _versions = MutableStateFlow<List<VersionEntry>>(emptyList())
    val versions = _versions.asStateFlow()
    private val _currentVersion = MutableStateFlow(0)
    val currentVersion = _currentVersion.asStateFlow()

    // Preview
    private val _previewUrl = MutableStateFlow("http://localhost:5173")
    val previewUrl = _previewUrl.asStateFlow()
    private val _previewDeviceSize = MutableStateFlow(DeviceSize.Phone)
    val previewDeviceSize = _previewDeviceSize.asStateFlow()
    private val _showConsole = MutableStateFlow(false)
    val showConsole = _showConsole.asStateFlow()
    private val _consoleLogs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val consoleLogs = _consoleLogs.asStateFlow()

    // Model
    private val _currentModelName = MutableStateFlow("Claude 3.5 Sonnet")
    val currentModelName = _currentModelName.asStateFlow()

    // Loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _messages.value = repository.loadMessages(projectId)
            _versions.value = repository.loadVersions(projectId)
            _currentVersion.value = _versions.value.lastIndex
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isAgentWorking.value) return

        _inputText.value = ""
        _isAgentWorking.value = true
        _agentStatus.value = "Thinking…"

        // Add user message
        val userMessage = ChatMessageData(role = ChatRole.User, content = text)
        _messages.value = _messages.value + userMessage

        // Run agent
        viewModelScope.launch {
            try {
                _agentStatus.value = "Planning…"
                val plan = agent.createPlan(text, projectId)
                _agentStatus.value = "Building…"
                val result = agent.executePlan(plan, projectId)
                _agentStatus.value = "Testing…"
                agent.runTests(projectId)

                // Add assistant response
                val assistantMessage = ChatMessageData(role = ChatRole.Assistant, content = result.summary)
                _messages.value = _messages.value + assistantMessage

                // Update versions
                val newVersion = VersionEntry(
                    summary = result.summary,
                    timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                    success = result.success,
                    filesChanged = result.filesChanged,
                    duration = result.duration
                )
                _versions.value = _versions.value + newVersion
                _currentVersion.value = _versions.value.lastIndex

            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessageData(role = ChatRole.Assistant, content = "Error: ${e.message}")
            } finally {
                _isAgentWorking.value = false
                _agentStatus.value = null
                _agentActions.value = emptyList()
            }
        }
    }

    fun undo() {
        if (_currentVersion.value > 0) {
            _currentVersion.value--
            // Restore project state
        }
    }

    fun restoreVersion(index: Int) {
        _currentVersion.value = index
        // Restore project state
    }

    fun attachImage() { /* TODO: Image picker */ }

    fun toggleVoiceInput() { /* TODO: Voice input */ }

    fun showModelSelector() { /* TODO: Model selector bottom sheet */ }

    fun reloadPreview() { /* TODO: Reload webview */ }

    fun setPreviewDeviceSize(size: DeviceSize) { _previewDeviceSize.value = size }

    fun openInBrowser() { /* TODO: Open in browser */ }

    fun toggleConsole() { _showConsole.value = !_showConsole.value }
}

import com.vibecoder.design.components.ChatMessageData
import com.vibecoder.design.components.ChatRole
import com.vibecoder.design.components.DeviceSize
import com.vibecoder.design.components.ToolAction
import com.vibecoder.design.components.ToolStatus
import com.vibecoder.design.components.VersionEntry
import com.vibecoder.design.components.ConsoleLog

// Placeholder interfaces - to be implemented in agent module
interface ChatRepository {
    fun loadMessages(projectId: String): List<ChatMessageData>
    fun loadVersions(projectId: String): List<VersionEntry>
}

interface Agent {
    data class Plan(val steps: List<String>)
    data class Result(val summary: String, val success: Boolean, val filesChanged: Int, val duration: Long)

    fun createPlan(prompt: String, projectId: String): Plan
    fun executePlan(plan: Plan, projectId: String): Result
    fun runTests(projectId: String)
}