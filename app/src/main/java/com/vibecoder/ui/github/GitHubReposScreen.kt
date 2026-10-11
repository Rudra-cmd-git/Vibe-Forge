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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GitHub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibecoder.R
import com.vibecoder.data.github.GitHubRepository
import com.vibecoder.ui.github.GitHubRepoDetailScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.ComposableViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun GitHubReposScreen(onRepoSelected: (GitHubRepository) -> Unit) {
    val viewModel: GitHubReposViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("GitHub Repositories") },
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.showCreateRepoDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Repository")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            
            when (state) {
                is GitHubReposState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GitHubReposState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Error: ${state.message}", textAlign = TextAlign.Center, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                is GitHubReposState.Success -> {
                    if (state.repositories.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Default.GitHub, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                                Text("No repositories yet", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                                Text("Create your first repository to get started", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(onClick = { viewModel.showCreateRepoDialog = true }) {
                                    Text("Create Repository")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.repositories) { repo ->
                                RepoCard(
                                    repo = repo,
                                    onClick = { onRepoSelected(repo) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Create repo dialog
        if (state is GitHubReposState.Success && viewModel.showCreateRepoDialog) {
            CreateRepoDialog(
                onDismiss = { viewModel.showCreateRepoDialog = false },
                onCreate = { name, description, isPrivate ->
                    viewModel.createRepository(name, description, isPrivate)
                }
            )
        }
    }
}

@Composable
fun RepoCard(repo: GitHubRepository, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repo icon
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = "",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            spacer(Modifier.width(16.dp))
            
            // Repo info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        repo.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                    if (repo.isPrivate) {
                        Icon(Icons.Default.Lock, contentDescription = "Private", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Public, contentDescription = "Public", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                
                repo.description?.let { desc ->
                    Text(desc, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (repo.language != null) {
                        Text("${repo.language}", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Text("⭐ ${repo.stargazersCount}", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text("🍴 ${repo.forksCount}", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            
            Icon(Icons.Default.ArrowBack, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun CreateRepoDialog(onDismiss: () -> Unit, onCreate: (String, String?, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Create New Repository", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            
            androidx.compose.material3.TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Repository name") },
                placeholder = { Text("my-awesome-project") },
                modifier = Modifier.fillMaxWidth()
            )
            
            androidx.compose.material3.TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Visibility", fontSize = 16.sp)
                androidx.compose.material3.Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isPrivate) "Private" else "Public", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Icon(if (isPrivate) Icons.Default.Lock else Icons.Default.Public, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss) { Text("Cancel") }
                spacer(Modifier.width(8.dp))
                Button(onClick = { onCreate(name, description.ifBlank { null }, isPrivate) }) { Text("Create") }
            }
        }
    }
}

@HiltViewModel
class GitHubReposViewModel @Inject constructor(
    private val gitHubRepository: com.vibecoder.data.github.GitHubRepository
) : ViewModel() {

    sealed interface UiState {
        data class Loading : UiState
        data class Success(val repositories: List<GitHubRepository>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState

    var showCreateRepoDialog: Boolean = false
        private set

    init {
        loadRepositories()
    }

    fun loadRepositories() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            gitHubRepository.listUserRepositories().onSuccess { repos ->
                _uiState.value = UiState.Success(repos)
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to load repositories")
            }
        }
    }

    fun refresh() {
        loadRepositories()
    }

    fun createRepository(name: String, description: String?, isPrivate: Boolean) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val request = com.vibecoder.data.github.CreateRepositoryRequest(
                name = name,
                description = description,
                private = isPrivate,
                autoInit = true
            )
            gitHubRepository.createRepository(request).onSuccess { repo ->
                showCreateRepoDialog = false
                loadRepositories()
            }.onFailure { e ->
                _uiState.value = UiState.Error("Failed to create repository: ${e.message}")
            }
        }
    }
}