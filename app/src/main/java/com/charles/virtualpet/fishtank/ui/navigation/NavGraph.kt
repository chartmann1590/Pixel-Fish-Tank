package com.charles.virtualpet.fishtank.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameScreen
import com.charles.virtualpet.fishtank.ui.settings.SettingsScreen
import com.charles.virtualpet.fishtank.ui.store.DecorationPlacementScreen
import com.charles.virtualpet.fishtank.ui.store.DecorationStoreScreen
import com.charles.virtualpet.fishtank.ui.tank.TankScreen

sealed class Screen(val route: String) {
    object Tank : Screen("tank")
    object MiniGame : Screen("minigame")
    object DecorationStore : Screen("decoration_store")
    object DecorationPlacement : Screen("decoration_placement")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: GameViewModel,
    repository: GameStateRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tank.route
    ) {
        composable(Screen.Tank.route) {
            TankScreen(
                viewModel = viewModel,
                onNavigateToMiniGame = {
                    navController.navigate(Screen.MiniGame.route)
                },
                onNavigateToStore = {
                    navController.navigate(Screen.DecorationStore.route)
                },
                onNavigateToPlacement = {
                    navController.navigate(Screen.DecorationPlacement.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.MiniGame.route) {
            MiniGameScreen(
                viewModel = viewModel,
                repository = repository,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.DecorationStore.route) {
            DecorationStoreScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.DecorationPlacement.route) {
            DecorationPlacementScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

