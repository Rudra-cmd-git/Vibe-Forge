package com.vibecoder.ui.projects

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
import androidx.compose.material.icons.filled.Add
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
import com.vibecoder.data.storage.Database
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.ComposableViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@Composable
fun ProjectListScreen(onProjectClick: (String) -> Unit, onNewProject: () -> Unit) {
    val viewModel: ProjectListViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = { /* navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            
            when (state) {
                is ProjectListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProjectListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Error: ${state.message}", textAlign = TextAlign.Center, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                is ProjectListState.Success -> {
                    if (state.projects.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                                Text("No projects yet", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                                Text("Create your first project to get started", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(onClick = onNewProject) {
                                    Text("Create Project")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.projects) { project ->
                                ProjectCard(project = project, onClick = { onProjectClick(project.id) })
                            }
                        }
                    }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = onNewProject,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Project")
        }
    }
}

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val database: Database
) : ViewModel() {

    sealed interface UiState {
        data class Loading : UiState
        data class Success(val projects: List<ProjectSummary>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState

    init {
        loadProjects()
    }

    fun loadProjects() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val projects = database.projectDao().getAllProjects().map { entity ->
                    ProjectSummary(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        lastOpened = entity.updatedAt,
                        githubRepo = entity.githubOwner?.let { "$it/${entity.githubRepoName}" }
                    )
                }.toList()
                _uiState.value = UiState.Success(projects)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load projects")
            }
        }
    }

    fun refresh() {
        loadProjects()
    }
}