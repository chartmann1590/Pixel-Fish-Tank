package com.charles.virtualpet.fishtank.ui.minigame.memoryshells

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.charles.virtualpet.fishtank.ui.minigame.DifficultySelector
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameDifficulty
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameEndScreen
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameResult
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.charles.virtualpet.fishtank.ui.minigame.common.Rewards
import com.charles.virtualpet.fishtank.ui.minigame.common.useGameTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.random.Random

enum class MemoryShellsGameState {
    WAITING,
    SHOWING, // Showing stars
    HIDDEN,  // Stars hidden, waiting for player input
    PLAYING, // Player can tap shells
    FINISHED
}

data class ShellState(
    val id: Int,
    var hasStar: Boolean = false,
    var isRevealed: Boolean = false
)

@Composable
fun MemoryShellsScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(MemoryShellsGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var numShells by remember { mutableIntStateOf(4) } // Start with 4 shells
    var previewTime by remember { mutableIntStateOf(1500) } // 1.5 seconds in ms
    var shells by remember { mutableStateOf<List<ShellState>>(emptyList()) }
    var correctShellIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var round by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == MemoryShellsGameState.PLAYING || gameState == MemoryShellsGameState.SHOWING || gameState == MemoryShellsGameState.HIDDEN
    )
    
    // Start new round
    fun startNewRound() {
        round++
        shells = (0 until numShells).map { ShellState(id = it) }
        
        // Select 1-2 shells to have stars (based on difficulty)
        val numStars = if (difficulty == MiniGameDifficulty.HARD && round > 2) 2 else 1
        correctShellIds = (0 until numShells).shuffled().take(numStars).toSet()
        
        correctShellIds.forEach { id ->
            shells = shells.map { if (it.id == id) it.copy(hasStar = true) else it }
        }
        
        gameState = MemoryShellsGameState.SHOWING
    }
    
    // Show stars, then hide - use separate LaunchedEffects to avoid restart issues
    LaunchedEffect(gameState == MemoryShellsGameState.SHOWING) {
        if (gameState == MemoryShellsGameState.SHOWING) {
            delay(previewTime.toLong())
            gameState = MemoryShellsGameState.HIDDEN
        }
    }
    
    // Separate LaunchedEffect for HIDDEN -> PLAYING transition
    LaunchedEffect(gameState == MemoryShellsGameState.HIDDEN) {
        if (gameState == MemoryShellsGameState.HIDDEN) {
            delay(200) // Brief pause before allowing input
            gameState = MemoryShellsGameState.PLAYING
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if ((gameState == MemoryShellsGameState.PLAYING || gameState == MemoryShellsGameState.SHOWING || gameState == MemoryShellsGameState.HIDDEN) 
            && timeRemaining == 0 && !isTimerActive) {
            gameState = MemoryShellsGameState.FINISHED
        }
    }
    
    // Handle shell tap
    fun onShellTap(shellId: Int) {
        if (gameState != MemoryShellsGameState.PLAYING) return
        
        val shell = shells.find { it.id == shellId } ?: return
        if (shell.isRevealed) return // Already revealed
        
        // Reveal shell immediately for visual feedback
        shells = shells.map { if (it.id == shellId) it.copy(isRevealed = true) else it }
        
        // Check if correct after a brief moment to show the reveal
        coroutineScope.launch {
            delay(500) // Brief pause to show the reveal
            
            if (correctShellIds.contains(shellId)) {
                // Correct!
                score += 10
                
                // Increase difficulty after correct rounds
                if (round % 2 == 0 && numShells < 8) {
                    numShells += 2
                }
                if (round % 3 == 0 && previewTime > 1000) {
                    previewTime -= 200 // Reduce preview time
                }
                
                // Start next round after showing success
                delay(1000)
                if (gameState == MemoryShellsGameState.PLAYING) {
                    startNewRound()
                }
            } else {
                // Wrong - show feedback, then continue
                delay(1000)
                if (gameState == MemoryShellsGameState.PLAYING) {
                    startNewRound()
                }
            }
        }
    }
    
    // Calculate result when game finishes
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == MemoryShellsGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.MEMORY_SHELLS)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.MEMORY_SHELLS, score)
            }
            
            MiniGameResult(
                type = MiniGameType.MEMORY_SHELLS,
                score = score,
                coinsEarned = coinsEarned,
                xpEarned = xpEarned,
                isHighScore = isHighScore
            )
        } else {
            null
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4),
                        Color(0xFFFFF59D),
                        Color(0xFFFFF176)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameState) {
                MemoryShellsGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "🐚",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Memory Shells",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.MEMORY_SHELLS)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Remember which shell hides the star!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    DifficultySelector(
                        selectedDifficulty = difficulty,
                        onDifficultySelected = { difficulty = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                    
                    Button(
                        onClick = {
                            gameState = MemoryShellsGameState.PLAYING
                            score = 0
                            round = 0
                            numShells = 4
                            previewTime = 1500
                            startNewRound()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Start Game 🎮",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                MemoryShellsGameState.SHOWING,
                MemoryShellsGameState.HIDDEN,
                MemoryShellsGameState.PLAYING -> {
                    // Timer and Score Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "⏱️ Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$timeRemaining",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeRemaining <= 5) Color.Red else Color(0xFFFFC107)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "🎯 Score",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$score",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFC107)
                                )
                            }
                        }
                    }
                    
                    // Instructions
                    Text(
                        text = when (gameState) {
                            MemoryShellsGameState.SHOWING -> "Watch carefully! ⭐"
                            MemoryShellsGameState.HIDDEN -> "Remember where the star was!"
                            MemoryShellsGameState.PLAYING -> "Tap the shell with the star!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Shell grid
                    val columns = when (numShells) {
                        4 -> 2
                        6 -> 3
                        8 -> 4
                        else -> 2
                    }
                    val rows = (numShells + columns - 1) / columns
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        repeat(rows) { row ->
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                repeat(columns) { col ->
                                    val index = row * columns + col
                                    if (index < numShells) {
                                        val shell = shells.getOrNull(index)
                                        if (shell != null) {
                                            ShellCard(
                                                shell = shell,
                                                isShowing = gameState == MemoryShellsGameState.SHOWING,
                                                onClick = { onShellTap(shell.id) },
                                                enabled = gameState == MemoryShellsGameState.PLAYING,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                MemoryShellsGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = MemoryShellsGameState.WAITING
                                score = 0
                                round = 0
                                numShells = 4
                                previewTime = 1500
                                shells = emptyList()
                                correctShellIds = emptySet()
                            },
                            onBack = {
                                onFinish(result)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellCard(
    shell: ShellState,
    isShowing: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled && !shell.isRevealed, onClick = onClick)
            .background(
                color = if (!enabled) Color(0xFFFFE082).copy(alpha = 0.5f) else Color(0xFFFFE082),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isShowing && shell.hasStar) {
            // Show star during preview
            Text(
                text = "⭐",
                style = MaterialTheme.typography.displaySmall
            )
        } else if (shell.isRevealed) {
            // Show revealed state - always show what's underneath
            if (shell.hasStar) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.displaySmall
                )
            } else {
                Text(
                    text = "🐚",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        } else {
            // Hidden shell - show closed shell
            Text(
                text = "🐚",
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

