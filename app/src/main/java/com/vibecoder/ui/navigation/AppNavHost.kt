package com.vibecoder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vibecoder.ui.home.HomeScreen
import com.vibecoder.ui.chat.ChatScreen
import com.vibecoder.ui.setup.SetupScreen
import com.vibecoder.ui.viewmodel.AppViewModel
import com.vibecoder.ui.viewmodel.ChatViewModel

@Composable
fun AppNavHost(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    val startDestination = if (appViewModel.isSetupComplete) "home" else "setup"

    NavHost(navController, startDestination = startDestination) {
        composable("setup") {
            SetupScreen(onSetupComplete = { appViewModel.markSetupComplete() })
        }
        composable("home") {
            HomeScreen(onNewProject = { projectId ->
                navController.navigate("chat/$projectId") {
                    popUpTo("home") { inclusive = true }
                }
            }, onOpenProject = { projectId ->
                navController.navigate("chat/$projectId") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
        composable(
            route = "chat/{projectId}",
            arguments = listOf(androidx.navigation.navArgument("projectId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.getString()!!
            val chatViewModel: ChatViewModel = hiltViewModel(key = projectId)
            ChatScreen(
                projectId = projectId,
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}