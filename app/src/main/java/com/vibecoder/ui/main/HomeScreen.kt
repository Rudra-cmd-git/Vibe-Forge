package com.vibecoder.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.vibecoder.R
import com.vibecoder.data.storage.Database
import com.vibecoder.ui.github.GitHubAuthScreen
import com.vibecoder.ui.projects.ProjectCard
import com.vibecoder.ui.projects.ProjectListScreen
import com.vibecoder.util.ThemeManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GitHub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource
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

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val projects = remember { mutableStateOf<List<ProjectSummary>>(emptyList()) }
    
    // TODO: Load projects from database
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero section
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VibeCoder",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                spacer(Modifier.height(8.dp))
                Text(
                    text = "AI-powered coding for everyone",
                    fontSize = 18.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                spacer(Modifier.height(8.dp))
                Text(
                    text = "Describe what you want. AI builds it. Push to GitHub in one tap.",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Project") },
                    title = "New Project",
                    subtitle = "Start from scratch",
                    onClick = { onNavigate("editor/new") }
                )
                QuickActionCard(
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Open Project") },
                    title = "Open Project",
                    subtitle = "Continue working",
                    onClick = { onNavigate("projects") }
                )
                QuickActionCard(
                    icon = { Icon(Icons.Default.GitHub, contentDescription = "GitHub") },
                    title = "GitHub",
                    subtitle = "Connect & sync",
                    onClick = { onNavigate("github/repos") }
                )
            }
            
            // Recent projects
            if (projects.value.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recent Projects", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("View All", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            .let { it -> androidx.compose.foundation.gestures.TapGestureDetector(onTap = { onNavigate("projects") }) }
                    }
                    spacer(Modifier.height(12.dp))
                    ProjectList(projects = projects.value, onProjectClick = { projectId ->
                        onNavigate("editor/$projectId")
                    })
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                            Text("No projects yet", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            Text("Create your first project to get started", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { onNavigate("editor/new") }) {
                                Text("Create Project")
                            }
                        }
                    }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = { onNavigate("editor/new") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Project")
        }
    }
}

@Composable
fun QuickActionCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(120.dp)
            .fillMaxSize(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            spacer(Modifier.height(8.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProjectList(projects: List<ProjectSummary>, onProjectClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(projects.take(5)) { project ->
            ProjectCard(project = project, onClick = { onProjectClick(project.id) })
        }
    }
}

data class ProjectSummary(
    val id: String,
    val name: String,
    val description: String?,
    val lastOpened: Long,
    val githubRepo: String?
)