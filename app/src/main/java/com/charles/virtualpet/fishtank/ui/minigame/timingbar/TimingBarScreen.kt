package com.charles.virtualpet.fishtank.ui.minigame.timingbar

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import com.charles.virtualpet.fishtank.ui.minigame.DifficultySelector
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameDifficulty
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameEndScreen
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameResult
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.charles.virtualpet.fishtank.ui.minigame.common.Rewards
import com.charles.virtualpet.fishtank.ui.minigame.common.useGameTimer
import kotlin.math.abs
import kotlinx.coroutines.delay

enum class TimingBarGameState {
    WAITING,
    PLAYING,
    FINISHED
}

@Composable
fun TimingBarScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(TimingBarGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var markerPosition by remember { mutableFloatStateOf(0.5f) } // 0.0 to 1.0
    var markerSpeed by remember { mutableFloatStateOf(0.02f) } // Speed per frame
    var barWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == TimingBarGameState.PLAYING
    )
    
    // Move marker - difficulty affects speed
    LaunchedEffect(gameState, difficulty) {
        if (gameState == TimingBarGameState.PLAYING) {
            markerPosition = 0.5f
            markerSpeed = 0.02f * difficulty.multiplier // Faster on harder difficulties
            score = 0
            while (gameState == TimingBarGameState.PLAYING) {
                delay(16) // ~60fps
                markerPosition += markerSpeed
                
                // Bounce at edges
                if (markerPosition >= 1.0f || markerPosition <= 0.0f) {
                    markerSpeed = -markerSpeed
                    markerPosition = markerPosition.coerceIn(0.0f, 1.0f)
                }
            }
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if (gameState == TimingBarGameState.PLAYING && timeRemaining == 0 && !isTimerActive) {
            gameState = TimingBarGameState.FINISHED
        }
    }
    
    // Calculate result when game finishes - apply difficulty multiplier to rewards
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == TimingBarGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.TIMING_BAR)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.TIMING_BAR, score)
            }
            
            MiniGameResult(
                type = MiniGameType.TIMING_BAR,
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
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0B2),
                        Color(0xFFFFCC80)
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
                TimingBarGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "⏱️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Timing Bar",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
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
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.TIMING_BAR)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Stop the marker at the perfect moment!",
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
                            gameState = TimingBarGameState.PLAYING
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
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
                
                TimingBarGameState.PLAYING -> {
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
                                    color = if (timeRemaining <= 5) Color.Red else Color(0xFFFF9800)
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
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Timing bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 32.dp)
                            .onSizeChanged { size ->
                                barWidth = size.width
                            }
                    ) {
                        // Background bar with gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFE0B2),
                                            Color(0xFFFFCC80),
                                            Color(0xFFFFB74D)
                                        )
                                    )
                                )
                        )
                        
                        // Center sweet spot indicator with glow effect - narrower on harder difficulties
                        val sweetSpotWidth = when (difficulty) {
                            MiniGameDifficulty.EASY -> 40.dp
                            MiniGameDifficulty.MEDIUM -> 30.dp
                            MiniGameDifficulty.HARD -> 20.dp
                        }
                        Box(
                            modifier = Modifier
                                .width(sweetSpotWidth)
                                .height(100.dp)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.5f),
                                            Color(0xFF66BB6A).copy(alpha = 0.3f),
                                            Color(0xFF4CAF50).copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )
                        
                        // Moving marker with gradient
                        if (barWidth > 0) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(100.dp)
                                    .align(Alignment.CenterStart)
                                    .offset(x = with(density) {
                                        val markerWidth = 16.dp.toPx()
                                        ((markerPosition * (barWidth - markerWidth))).dp
                                    })
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFF9800),
                                                Color(0xFFFF6F00),
                                                Color(0xFFE65100)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // STOP button with gradient
                    Button(
                        onClick = {
                            // Calculate score based on distance from center - harder difficulties have narrower sweet spot
                            val sweetSpotWidth = when (difficulty) {
                                MiniGameDifficulty.EASY -> 0.2f // 20% of bar width
                                MiniGameDifficulty.MEDIUM -> 0.15f // 15% of bar width
                                MiniGameDifficulty.HARD -> 0.1f // 10% of bar width
                            }
                            val distanceFromCenter = abs(markerPosition - 0.5f)
                            val maxDistance = sweetSpotWidth / 2f
                            val attemptScore = if (distanceFromCenter <= maxDistance) {
                                100 // Perfect hit
                            } else {
                                (100 - ((distanceFromCenter - maxDistance) * 200 / (0.5f - maxDistance)).toInt()).coerceIn(0, 100)
                            }
                            score += attemptScore
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "🛑 STOP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                TimingBarGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = TimingBarGameState.WAITING
                                score = 0
                                markerPosition = 0.5f
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
