package com.charles.virtualpet.fishtank.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charles.virtualpet.fishtank.audio.BackgroundMusicManager
import com.charles.virtualpet.fishtank.audio.SfxManager
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameHubScreen
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameRegistry
import com.charles.virtualpet.fishtank.ui.minigame.DataStoreHighScoreStore
import com.charles.virtualpet.fishtank.ui.minigame.bubblepop.BubblePopScreen
import com.charles.virtualpet.fishtank.ui.minigame.timingbar.TimingBarScreen
import com.charles.virtualpet.fishtank.ui.minigame.cleanuprush.CleanupRushScreen
import com.charles.virtualpet.fishtank.ui.minigame.fooddrop.FoodDropScreen
import com.charles.virtualpet.fishtank.ui.minigame.memoryshells.MemoryShellsScreen
import com.charles.virtualpet.fishtank.ui.minigame.fishfollow.FishFollowScreen
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
    object FoodDrop : Screen("food_drop")
    object MemoryShells : Screen("memory_shells")
    object FishFollow : Screen("fish_follow")
    object DecorationStore : Screen("decoration_store")
    object DecorationPlacement : Screen("decoration_placement")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: GameViewModel,
    repository: GameStateRepository,
    sfxManager: SfxManager?,
    bgMusicManager: BackgroundMusicManager?
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tank.route
    ) {
        composable(Screen.Tank.route) {
            TankScreen(
                viewModel = viewModel,
                sfxManager = sfxManager,
                bgMusicManager = bgMusicManager,
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
                    val route = when (gameType) {
                        MiniGameType.BUBBLE_POP -> Screen.BubblePop.route
                        MiniGameType.TIMING_BAR -> Screen.TimingBar.route
                        MiniGameType.CLEANUP_RUSH -> Screen.CleanupRush.route
                        MiniGameType.FOOD_DROP -> Screen.FoodDrop.route
                        MiniGameType.MEMORY_SHELLS -> Screen.MemoryShells.route
                        MiniGameType.FISH_FOLLOW -> Screen.FishFollow.route
                    }
                    navController.navigate(route)
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
                sfxManager = sfxManager,
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
        
        composable(Screen.FoodDrop.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            FoodDropScreen(
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
        
        composable(Screen.MemoryShells.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            MemoryShellsScreen(
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
        
        composable(Screen.FishFollow.route) {
            val highScoreStore = remember { DataStoreHighScoreStore(repository) }
            FishFollowScreen(
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
                sfxManager = sfxManager,
                bgMusicManager = bgMusicManager,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

