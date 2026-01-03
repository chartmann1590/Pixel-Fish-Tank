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
import com.charles.virtualpet.fishtank.ui.tutorial.TutorialOnboardingScreen
import com.charles.virtualpet.fishtank.analytics.AnalyticsHelper
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

sealed class Screen(val route: String) {
    object Tutorial : Screen("tutorial")
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
    storeRepository: com.charles.virtualpet.fishtank.data.FirebaseStoreRepository?,
    sfxManager: SfxManager?,
    bgMusicManager: BackgroundMusicManager?
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val hasCompletedTutorial = gameState.settings.hasCompletedTutorial
    
    // Determine start destination based on tutorial completion
    val startDestination = if (hasCompletedTutorial) {
        Screen.Tank.route
    } else {
        Screen.Tutorial.route
    }
    
    // Navigate to tutorial if not completed (handles case where user navigates away and comes back)
    LaunchedEffect(hasCompletedTutorial) {
        if (!hasCompletedTutorial && navController.currentDestination?.route != Screen.Tutorial.route) {
            navController.navigate(Screen.Tutorial.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Tutorial.route) {
            // Check if this is a replay (coming from Settings)
            val isReplay = navController.previousBackStackEntry?.destination?.route == Screen.Settings.route
            
            TutorialOnboardingScreen(
                onComplete = {
                    if (!isReplay) {
                        // Only mark as completed if it's the first time
                        viewModel.completeTutorial()
                    }
                    navController.navigate(Screen.Tank.route) {
                        popUpTo(Screen.Tutorial.route) { inclusive = true }
                    }
                },
                isReplay = isReplay
            )
        }
        
        composable(Screen.Tank.route) {
            // Check if we're coming from tutorial to show guided tour
            val showGuidedTour = navController.previousBackStackEntry?.destination?.route == Screen.Tutorial.route
            
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
                },
                showGuidedTourOnStart = showGuidedTour
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                    // Log analytics
                    AnalyticsHelper.logMiniGameComplete(
                        gameType = result.type.name,
                        difficulty = result.difficulty.name,
                        score = result.score,
                        coinsEarned = result.coinsEarned,
                        xpEarned = result.xpEarned
                    )
                    if (result.isHighScore) {
                        AnalyticsHelper.logMiniGameHighScore(
                            gameType = result.type.name,
                            difficulty = result.difficulty.name,
                            highScore = result.score
                        )
                    }
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
                repository = storeRepository,
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
                },
                onReplayTutorial = {
                    navController.navigate(Screen.Tutorial.route)
                }
            )
        }
    }
}

