package com.vibecoder.ui.settings

import android.content.Context
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
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
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.sections) { section ->
                    SettingsSection(section = section)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(section: SettingsSectionModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(section.title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 16.dp))
            
            section.items.forEach { item ->
                SettingsItem(item = item)
            }
            
            spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsItem(item: SettingsItemModel) {
    when (item) {
        is SettingsItemModel.ToggleItem -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.title, fontSize = 16.sp)
                    item.subtitle?.let { Text(it, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Switch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        is SettingsItemModel.ActionItem -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.title, fontSize = 16.sp, color = item.textColor)
                    item.subtitle?.let { Text(it, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                if (item.icon != null) {
                    Icon(item.icon!!, contentDescription = "", tint = item.textColor, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.ArrowBack, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                }
            }.let { 
                androidx.compose.foundation.gestures.TapGestureDetector(onTap = item.onClick)
            }
        }
        is SettingsItemModel.InfoItem -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.title, fontSize = 16.sp)
                    item.subtitle?.let { Text(it, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Text(item.value, fontSize = 16.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

sealed interface SettingsItemModel {
    data class ToggleItem(
        val title: String,
        val subtitle: String?,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItemModel
    
    data class ActionItem(
        val title: String,
        val subtitle: String?,
        val icon: androidx.compose.ui.graphics.painter.Painter?,
        val textColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        val onClick: () -> Unit
    ) : SettingsItemModel
    
    data class InfoItem(
        val title: String,
        val subtitle: String?,
        val value: String
    ) : SettingsItemModel
}

data class SettingsSectionModel(
    val title: String,
    val items: List<SettingsItemModel>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: com.vibecoder.data.storage.PreferencesManager,
    private val gitHubRepository: com.vibecoder.data.github.GitHubRepository
) : ViewModel() {

    data class UiState(
        val sections: List<SettingsSectionModel>
    )

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState = _uiState

    private fun buildUiState(): UiState {
        return UiState(sections = listOf(
            SettingsSectionModel(
                title = "GitHub",
                items = listOf(
                    SettingsItemModel.ActionItem(
                        title = "Connected Account",
                        subtitle = "Manage GitHub connection",
                        icon = androidx.compose.material.icons.filled.GitHub.painterResource(),
                        onClick = { /* navigate to GitHub settings */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Disconnect GitHub",
                        subtitle = "Remove stored credentials",
                        icon = androidx.compose.material.icons.filled.Delete.painterResource(),
                        textColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        onClick = { gitHubRepository.disconnect() }
                    )
                )
            ),
            SettingsSectionModel(
                title = "Appearance",
                items = listOf(
                    SettingsItemModel.ActionItem(
                        title = "Theme",
                        subtitle = "Light, Dark, or System",
                        icon = Icons.Default.Palette.painterResource(),
                        onClick = { /* show theme picker */ }
                    ),
                    SettingsItemModel.InfoItem(
                        title = "Current Theme",
                        subtitle = "System default",
                        value = "System"
                    )
                )
            ),
            SettingsSectionModel(
                title = "Security & Privacy",
                items = listOf(
                    SettingsItemModel.ToggleItem(
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint/face to open app",
                        checked = preferencesManager.biometricEnabled.value,
                        onCheckedChange = { preferencesManager.setBiometricEnabled(it) }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Auto-lock Timeout",
                        subtitle = "Lock app after inactivity",
                        icon = Icons.Default.Security.painterResource(),
                        onClick = { /* show timeout picker */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Clear All Data",
                        subtitle = "Remove all projects, tokens, and settings",
                        icon = Icons.Default.Delete.painterResource(),
                        textColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        onClick = { /* show confirmation dialog */ }
                    )
                )
            ),
            SettingsSectionModel(
                title = "Editor",
                items = listOf(
                    SettingsItemModel.ToggleItem(
                        title = "Auto-commit",
                        subtitle = "Automatically commit after AI changes",
                        checked = preferencesManager.autoCommit.value,
                        onCheckedChange = { preferencesManager.setAutoCommit(it) }
                    ),
                    SettingsItemModel.ToggleItem(
                        title = "Auto-push",
                        subtitle = "Automatically push to GitHub after commit",
                        checked = preferencesManager.autoPush.value,
                        onCheckedChange = { preferencesManager.setAutoPush(it) }
                    ),
                    SettingsItemModel.ToggleItem(
                        title = "Show Line Numbers",
                        subtitle = "Display line numbers in editor",
                        checked = preferencesManager.showLineNumbers.value,
                        onCheckedChange = { preferencesManager.setShowLineNumbers(it) }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Font Size",
                        subtitle = "${preferencesManager.codeFontSize.value}sp",
                        icon = Icons.Default.TextFields.painterResource(),
                        onClick = { /* show font size picker */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Tab Size",
                        subtitle = "${preferencesManager.tabSize.value} spaces",
                        icon = Icons.Default.FormatIndentIncrease.painterResource(),
                        onClick = { /* show tab size picker */ }
                    )
                )
            ),
            SettingsSectionModel(
                title = "AI Agent",
                items = listOf(
                    SettingsItemModel.ActionItem(
                        title = "Model",
                        subtitle = preferencesManager.agentModel.value,
                        icon = Icons.Default.Psychology.painterResource(),
                        onClick = { /* show model picker */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Temperature",
                        subtitle = "${preferencesManager.agentTemperature.value}",
                        icon = Icons.Default.Tune.painterResource(),
                        onClick = { /* show temperature slider */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Max Tokens",
                        subtitle = "${preferencesManager.agentMaxTokens.value}",
                        icon = Icons.Default.FormatListNumbered.painterResource(),
                        onClick = { /* show max tokens picker */ }
                    )
                )
            ),
            SettingsSectionModel(
                title = "Storage",
                items = listOf(
                    SettingsItemModel.InfoItem(
                        title = "Cache Size",
                        subtitle = "Temporary files and build artifacts",
                        value = "Calculating..."
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        icon = Icons.Default.Delete.painterResource(),
                        onClick = { /* clear cache */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Export Data",
                        subtitle = "Backup projects and settings",
                        icon = Icons.Default.Download.painterResource(),
                        onClick = { /* export data */ }
                    )
                )
            ),
            SettingsSectionModel(
                title = "About",
                items = listOf(
                    SettingsItemModel.InfoItem(
                        title = "Version",
                        subtitle = "App version",
                        value = "1.0.0"
                    ),
                    SettingsItemModel.InfoItem(
                        title = "Build",
                        subtitle = "Build number",
                        value = "1"
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Open Source Licenses",
                        subtitle = "View third-party licenses",
                        icon = Icons.Default.Description.painterResource(),
                        onClick = { /* show licenses */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Privacy Policy",
                        subtitle = "Read our privacy policy",
                        icon = Icons.Default.PrivacyTip.painterResource(),
                        onClick = { /* open privacy policy */ }
                    ),
                    SettingsItemModel.ActionItem(
                        title = "Report an Issue",
                        subtitle = "Help us improve VibeCoder",
                        icon = Icons.Default.BugReport.painterResource(),
                        onClick = { /* open issue tracker */ }
                    )
                )
            )
        ))
    }
}