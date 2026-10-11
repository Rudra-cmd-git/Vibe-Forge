package com.vibecoder.ui.github

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Copy
import androidx.compose.material.icons.filled.GitHub
import androidx.compose.material.icons.filled.Info
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
fun GitHubAuthScreen(onAuthComplete: () -> Unit) {
    val viewModel: GitHubAuthViewModel = hiltViewModel()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top app bar
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("Connect GitHub") },
                navigationIcon = {
                    IconButton(onClick = { onAuthComplete() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // GitHub logo
                Icon(
                    painterResource(id = com.vibecoder.R.drawable.ic_github),
                    contentDescription = "GitHub",
                    modifier = Modifier.size(80.dp),
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                
                when (authState) {
                    is GitHubAuthState.Idle -> {
                        IdleAuthView(onStartOAuth = { viewModel.startDeviceFlow() }, onUsePAT = { viewModel.showPATDialog = true })
                    }
                    is GitHubAuthState.Loading -> {
                        LoadingAuthView(message = authState.message)
                    }
                    is GitHubAuthState.DeviceCode -> {
                        DeviceCodeAuthView(
                            deviceCode = authState.deviceCode,
                            userCode = authState.userCode,
                            verificationUri = authState.verificationUri,
                            expiresIn = authState.expiresIn,
                            interval = authState.interval,
                            onCopyCode = { viewModel.copyUserCode() },
                            onOpenBrowser = { viewModel.openBrowser() }
                        )
                    }
                    is GitHubAuthState.Polling -> {
                        PollingAuthView(
                            userCode = authState.userCode,
                            progress = authState.progress,
                            onCancel = { viewModel.cancelPolling() }
                        )
                    }
                    is GitHubAuthState.Success -> {
                        SuccessAuthView(
                            username = authState.username,
                            onContinue = { onAuthComplete() }
                        )
                    }
                    is GitHubAuthState.Error -> {
                        ErrorAuthView(
                            message = authState.message,
                            onRetry = { viewModel.startDeviceFlow() },
                            onUsePAT = { viewModel.showPATDialog = true }
                        )
                    }
                    is GitHubAuthState.PATDialog -> {
                        PATDialogView(
                            onSubmit = { token -> viewModel.submitPAT(token) },
                            onCancel = { viewModel.cancelPATDialog() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IdleAuthView(onStartOAuth: () -> Unit, onUsePAT: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Connect your GitHub account to sync repositories",
            textAlign = TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onStartOAuth, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GitHub, contentDescription = "", modifier = Modifier.size(24.dp))
                spacer(Modifier.width(8.dp))
                Text("Connect with GitHub (OAuth)", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        Text("or", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Button(
            onClick = onUsePAT,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Text("Use Personal Access Token", fontSize = 16.sp)
        }
    }
}

@Composable
fun LoadingAuthView(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.CircularProgressIndicator()
        Text(message, textAlign = TextAlign.Center)
    }
}

@Composable
fun DeviceCodeAuthView(
    deviceCode: String,
    userCode: String,
    verificationUri: String,
    expiresIn: Int,
    interval: Int,
    onCopyCode: () -> Unit,
    onOpenBrowser: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Step 1: Copy this code", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        userCode,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    )
                    IconButton(onClick = onCopyCode) {
                        Icon(Icons.Default.Copy, contentDescription = "Copy code")
                    }
                }
                Text("Code copied!", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Step 2: Open in browser", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                spacer(Modifier.height(8.dp))
                Text(verificationUri, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                spacer(Modifier.height(16.dp))
                Button(onClick = onOpenBrowser, modifier = Modifier.fillMaxWidth()) {
                    Text("Open github.com/login/device")
                }
            }
        }
        
        Text(
            "This code expires in ${expiresIn / 60} minutes",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PollingAuthView(userCode: String, progress: Float, onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Waiting for authorization...", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text("Code: $userCode", fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp))
        
        Text("Please approve in your browser", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        
        Button(onClick = onCancel, colors = androidx.compose.material3.ButtonDefaults.textButtonColors()) {
            Text("Cancel")
        }
    }
}

@Composable
fun SuccessAuthView(username: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.success, modifier = Modifier.size(64.dp))
        Text("Connected!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Welcome, @$username", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        spacer(Modifier.height(8.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().width(200.dp)) {
            Text("Continue")
        }
    }
}

@Composable
fun ErrorAuthView(message: String, onRetry: () -> Unit, onUsePAT: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Close, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Text("Connection Failed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        Text(message, textAlign = TextAlign.Center, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("Try Again")
            }
            Button(onClick = onUsePAT, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Text("Use Personal Access Token Instead")
            }
        }
    }
}

@Composable
fun PATDialogView(onSubmit: (String) -> Unit, onCancel: () -> Unit) {
    var token by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Personal Access Token", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Text("Paste a GitHub Personal Access Token (classic) with 'repo' scope", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                placeholder = { Text("ghp_...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onCancel) { Text("Cancel") }
                spacer(Modifier.width(8.dp))
                Button(onClick = { onSubmit(token) }, modifier = Modifier.fillMaxWidth().width(140.dp)) {
                    Text("Save")
                }
            }
        }
    }
}

@HiltViewModel
class GitHubAuthViewModel @Inject constructor(
    private val gitHubRepository: com.vibecoder.data.github.GitHubRepository,
    private val preferencesManager: com.vibecoder.data.storage.PreferencesManager
) : ViewModel() {

    sealed interface AuthState {
        data class Idle : AuthState
        data class Loading(val message: String) : AuthState
        data class DeviceCode(
            val deviceCode: String,
            val userCode: String,
            val verificationUri: String,
            val expiresIn: Int,
            val interval: Int
        ) : AuthState
        data class Polling(
            val userCode: String,
            val progress: Float
        ) : AuthState
        data class Success(val username: String) : AuthState
        data class Error(val message: String) : AuthState
        data class PATDialog : AuthState
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState

    private var pollingJob: kotlinx.coroutines.Job? = null
    var showPATDialog: Boolean = false
        private set

    fun startDeviceFlow() {
        _authState.value = AuthState.Loading("Starting device flow...")
        viewModelScope.launch {
            val result = gitHubRepository.startDeviceFlow()
            result.onSuccess { response ->
                _authState.value = AuthState.DeviceCode(
                    deviceCode = response.deviceCode,
                    userCode = response.userCode,
                    verificationUri = response.verificationUri,
                    expiresIn = response.expiresIn,
                    interval = response.interval
                )
                startPolling(response.deviceCode, response.interval)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Failed to start device flow")
            }
        }
    }

    private fun startPolling(deviceCode: String, interval: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var elapsed = 0
            while (true) {
                _authState.value = AuthState.Polling(userCode = "", progress = (elapsed % 300) / 300f)
                delay(interval * 1000L)
                elapsed += interval
                
                val result = gitHubRepository.pollForToken(deviceCode)
                result.onSuccess { tokenResponse ->
                    pollingJob?.cancel()
                    gitHubRepository.saveTokenFromResponse(tokenResponse).onSuccess { tokenData ->
                        gitHubRepository.getAuthenticatedUser().onSuccess { user ->
                            _authState.value = AuthState.Success(user.login)
                        }.onFailure { e ->
                            _authState.value = AuthState.Error("Failed to get user: ${e.message}")
                        }
                    }.onFailure { e ->
                        _authState.value = AuthState.Error("Failed to save token: ${e.message}")
                    }
                    return@launch
                }.onFailure {
                    when (it) {
                        is com.vibecoder.data.github.GitHubRepository.GitHubAuthException.Pending -> continue
                        is com.vibecoder.data.github.GitHubRepository.GitHubAuthException.SlowDown -> delay(5000)
                        is com.vibecoder.data.github.GitHubRepository.GitHubAuthException.Expired -> {
                            _authState.value = AuthState.Error("Device code expired. Please try again.")
                            return@launch
                        }
                        is com.vibecoder.data.github.GitHubRepository.GitHubAuthException.Denied -> {
                            _authState.value = AuthState.Error("Authorization denied.")
                            return@launch
                        }
                        else -> {
                            _authState.value = AuthState.Error(it.message ?: "Authentication failed")
                            return@launch
                        }
                    }
                }
            }
        }
    }

    fun cancelPolling() {
        pollingJob?.cancel()
        _authState.value = AuthState.Idle
    }

    fun copyUserCode() {
        // Copy to clipboard
    }

    fun openBrowser() {
        // Open browser intent
    }

    fun submitPAT(token: String) {
        _authState.value = AuthState.Loading("Validating token...")
        viewModelScope.launch {
            gitHubRepository.savePersonalAccessToken(token).onSuccess {
                gitHubRepository.getAuthenticatedUser().onSuccess { user ->
                    _authState.value = AuthState.Success(user.login)
                }.onFailure { e ->
                    _authState.value = AuthState.Error("Invalid token: ${e.message}")
                }
            }.onFailure { e ->
                _authState.value = AuthState.Error("Failed to save token: ${e.message}")
            }
        }
    }

    fun cancelPATDialog() {
        showPATDialog = false
        _authState.value = AuthState.Idle
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}