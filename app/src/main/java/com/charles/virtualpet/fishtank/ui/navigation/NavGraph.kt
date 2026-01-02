package com.charles.virtualpet.fishtank.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameHubScreen
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.charles.virtualpet.fishtank.ui.minigame.DataStoreHighScoreStore
import com.charles.virtualpet.fishtank.ui.minigame.bubblepop.BubblePopScreen
import com.charles.virtualpet.fishtank.ui.minigame.timingbar.TimingBarScreen
import com.charles.virtualpet.fishtank.ui.minigame.cleanuprush.CleanupRushScreen
import com.charles.virtualpet.fishtank.ui.settings.SettingsScreen
import com.charles.virtualpet.fishtank.ui.store.DecorationPlacementScreen
import com.charles.virtualpet.fishtank.ui.store.DecorationStoreScreen
import com.charles.virtualpet.fishtank.ui.tank.TankScreen

sealed class Screen(val route: String) {
    object Tank : Screen("tank")
    object MiniGameHub : Screen("minigame_hub")
    object BubblePop : Screen("bubble_pop")
    object TimingBar : Screen("timing_bar")
    object CleanupRush : Screen("cleanup_rush")
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
                    navController.navigate(Screen.MiniGameHub.route)
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
        
        composable(Screen.MiniGameHub.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            MiniGameHubScreen(
                highScoreStore = highScoreStore,
                onStartGame = { gameType ->
                    when (gameType) {
                        MiniGameType.BUBBLE_POP -> navController.navigate(Screen.BubblePop.route)
                        MiniGameType.TIMING_BAR -> navController.navigate(Screen.TimingBar.route)
                        MiniGameType.CLEANUP_RUSH -> navController.navigate(Screen.CleanupRush.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BubblePop.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            BubblePopScreen(
                highScoreStore = highScoreStore,
                onFinish = { result ->
                    viewModel.addCoins(result.coinsEarned)
                    viewModel.addXP(result.xpEarned)
                    viewModel.completeMinigameTask()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TimingBar.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            TimingBarScreen(
                highScoreStore = highScoreStore,
                onFinish = { result ->
                    viewModel.addCoins(result.coinsEarned)
                    viewModel.addXP(result.xpEarned)
                    viewModel.completeMinigameTask()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CleanupRush.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            CleanupRushScreen(
                highScoreStore = highScoreStore,
                onFinish = { result ->
                    viewModel.addCoins(result.coinsEarned)
                    viewModel.addXP(result.xpEarned)
                    viewModel.completeMinigameTask()
                    navController.popBackStack()
                },
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
                repository = repository,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

