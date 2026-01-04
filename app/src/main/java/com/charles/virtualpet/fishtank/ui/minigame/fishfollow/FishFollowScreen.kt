package com.charles.virtualpet.fishtank.ui.minigame.fishfollow

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
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
import com.charles.virtualpet.fishtank.ui.components.AdMobBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.random.Random

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}

enum class FishFollowGameState {
    WAITING,
    SHOWING,  // Showing sequence
    PLAYING,  // Player input
    FINISHED
}

@Composable
fun FishFollowScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(FishFollowGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var sequenceLength by remember { mutableIntStateOf(3) } // Start with 3
    var currentSequence by remember { mutableStateOf<List<Direction>>(emptyList()) }
    var playerInput by remember { mutableStateOf<List<Direction>>(emptyList()) }
    var showingIndex by remember { mutableIntStateOf(0) }
    var highlightedDirection by remember { mutableStateOf<Direction?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == FishFollowGameState.PLAYING || gameState == FishFollowGameState.SHOWING
    )
    
    // Generate new sequence
    fun generateSequence() {
        currentSequence = (0 until sequenceLength).map {
            Direction.values()[Random.nextInt(Direction.values().size)]
        }
        showingIndex = 0
        playerInput = emptyList()
        gameState = FishFollowGameState.SHOWING
    }
    
    // Show sequence - difficulty affects display speed
    LaunchedEffect(gameState, currentSequence.size, difficulty) {
        if (gameState == FishFollowGameState.SHOWING && currentSequence.isNotEmpty()) {
            while (showingIndex < currentSequence.size && gameState == FishFollowGameState.SHOWING) {
                highlightedDirection = currentSequence[showingIndex]
                delay((800L / difficulty.multiplier).toLong()) // Flash time - faster on harder difficulties
                highlightedDirection = null
                delay((200L / difficulty.multiplier).toLong()) // Brief pause between flashes - faster on harder difficulties
                showingIndex++
            }
            // Sequence shown, now wait for player input
            delay((300L / difficulty.multiplier).toLong())
            if (gameState == FishFollowGameState.SHOWING) {
                gameState = FishFollowGameState.PLAYING
                showingIndex = 0
            }
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if ((gameState == FishFollowGameState.PLAYING || gameState == FishFollowGameState.SHOWING)
            && timeRemaining == 0 && !isTimerActive) {
            gameState = FishFollowGameState.FINISHED
        }
    }
    
    // Handle direction tap
    fun onDirectionTap(direction: Direction) {
        if (gameState != FishFollowGameState.PLAYING) return
        
        playerInput = playerInput + direction
        highlightedDirection = direction
        
        // Check if input matches sequence so far
        val inputIndex = playerInput.size - 1
        if (inputIndex < currentSequence.size) {
            if (playerInput[inputIndex] == currentSequence[inputIndex]) {
                // Correct so far
                if (playerInput.size == currentSequence.size) {
                    // Complete sequence correct!
                    score += 10
                    
                    // Increase sequence length (max 6)
                    if (sequenceLength < 6) {
                        sequenceLength++
                    }
                    
                    // Generate next sequence after short delay
                    coroutineScope.launch {
                        delay(500)
                        highlightedDirection = null
                        delay(500)
                        if (gameState == FishFollowGameState.PLAYING) {
                            generateSequence()
                        }
                    }
                }
            } else {
                // Wrong direction - reset to length 3 (cozy, not punishing)
                sequenceLength = 3
                playerInput = emptyList()
                highlightedDirection = null
                
                // Generate new sequence after short delay
                coroutineScope.launch {
                    delay(1000)
                    if (gameState == FishFollowGameState.PLAYING) {
                        generateSequence()
                    }
                }
            }
        }
    }
    
    // Calculate result when game finishes
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == FishFollowGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.FISH_FOLLOW)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.FISH_FOLLOW, score)
            }
            
            MiniGameResult(
                type = MiniGameType.FISH_FOLLOW,
                score = score,
                coinsEarned = coinsEarned,
                xpEarned = xpEarned,
                isHighScore = isHighScore,
                difficulty = difficulty
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
                        Color(0xFFE8F5E9),
                        Color(0xFFC8E6C9),
                        Color(0xFFA5D6A7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 58.dp), // Space for banner (50dp) + padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameState) {
                FishFollowGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "🐟",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Fish Follow",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
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
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.FISH_FOLLOW)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Repeat the sequence of directions!",
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
                            gameState = FishFollowGameState.PLAYING
                            score = 0
                            sequenceLength = 3
                            generateSequence()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
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
                
                FishFollowGameState.SHOWING,
                FishFollowGameState.PLAYING -> {
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
                                    color = if (timeRemaining <= 5) Color.Red else Color(0xFF4CAF50)
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
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                    
                    // Instructions
                    Text(
                        text = when (gameState) {
                            FishFollowGameState.SHOWING -> "Watch the sequence! 👀"
                            FishFollowGameState.PLAYING -> "Repeat the sequence! 🎯"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Direction buttons in cross layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        // UP button
                        DirectionButton(
                            direction = Direction.UP,
                            isHighlighted = highlightedDirection == Direction.UP,
                            onClick = { onDirectionTap(Direction.UP) },
                            enabled = gameState == FishFollowGameState.PLAYING,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                        ) {
                            // LEFT button
                            DirectionButton(
                                direction = Direction.LEFT,
                                isHighlighted = highlightedDirection == Direction.LEFT,
                                onClick = { onDirectionTap(Direction.LEFT) },
                                enabled = gameState == FishFollowGameState.PLAYING,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            
                            // RIGHT button
                            DirectionButton(
                                direction = Direction.RIGHT,
                                isHighlighted = highlightedDirection == Direction.RIGHT,
                                onClick = { onDirectionTap(Direction.RIGHT) },
                                enabled = gameState == FishFollowGameState.PLAYING,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // DOWN button
                        DirectionButton(
                            direction = Direction.DOWN,
                            isHighlighted = highlightedDirection == Direction.DOWN,
                            onClick = { onDirectionTap(Direction.DOWN) },
                            enabled = gameState == FishFollowGameState.PLAYING,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                FishFollowGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = FishFollowGameState.WAITING
                                score = 0
                                sequenceLength = 3
                                currentSequence = emptyList()
                                playerInput = emptyList()
                                showingIndex = 0
                                highlightedDirection = null
                            },
                            onBack = {
                                onFinish(result)
                            }
                        )
                    }
                }
            }
            
            // AdMob Banner
            AdMobBanner()
        }
    }
}

@Composable
private fun DirectionButton(
    direction: Direction,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.2f else 1f,
        animationSpec = tween(200),
        label = "direction_highlight"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(200),
        label = "direction_enabled"
    )
    
    val (emoji, color) = when (direction) {
        Direction.LEFT -> "⬅️" to Color(0xFFFF9800)
        Direction.RIGHT -> "➡️" to Color(0xFF2196F3)
        Direction.UP -> "⬆️" to Color(0xFF4CAF50)
        Direction.DOWN -> "⬇️" to Color(0xFFE91E63)
    }
    
    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .alpha(alpha)
            .clickable(enabled = enabled, onClick = onClick)
            .background(
                color = if (isHighlighted) {
                    color.copy(alpha = 0.8f)
                } else {
                    color.copy(alpha = 0.4f)
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displaySmall
        )
    }
}

