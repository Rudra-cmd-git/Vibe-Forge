package com.vibecoder.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.ComposableViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vibecoder.R
import com.vibecoder.ui.editor.EditorScreen
import com.vibecoder.ui.github.GitHubAuthScreen
import com.vibecoder.ui.github.GitHubReposScreen
import com.vibecoder.ui.github.GitHubRepoDetailScreen
import com.vibecoder.ui.settings.SettingsScreen
import com.vibecoder.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContent {
            MaterialTheme {
                Surface {
                    VibeCoderNavHost(viewModel)
                }
            }
        }
    }
}

@Composable
fun VibeCoderNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable("editor/{projectId}") {
            val projectId = it.getString("projectId") ?: ""
            EditorScreen(projectId = projectId, onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable("github/auth") {
            GitHubAuthScreen(onAuthComplete = {
                navController.popBackStack("home", inclusive = false)
            })
        }
        composable("github/repos") {
            GitHubReposScreen(onRepoSelected = { repo ->
                navController.navigate("github/repo/${repo.id}")
            })
        }
        composable("github/repo/{repoId}") {
            val repoId = it.getLong("repoId", 0L)
            GitHubRepoDetailScreen(repoId = repoId)
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    // Home screen implementation
}