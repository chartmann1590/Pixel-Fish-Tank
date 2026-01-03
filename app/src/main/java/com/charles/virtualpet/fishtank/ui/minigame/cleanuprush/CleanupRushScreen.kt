package com.charles.virtualpet.fishtank.ui.minigame.cleanuprush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.charles.virtualpet.fishtank.ui.minigame.DifficultySelector
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameDifficulty
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameEndScreen
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameResult
import com.charles.virtualpet.fishtank.audio.SfxEvent
import com.charles.virtualpet.fishtank.audio.SfxManager
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.charles.virtualpet.fishtank.ui.minigame.common.Rewards
import com.charles.virtualpet.fishtank.ui.minigame.common.useGameTimer
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay

data class AlgaeSpot(
    val id: String,
    val x: Float,
    val y: Float,
    val size: Float
)

enum class CleanupRushGameState {
    WAITING,
    PLAYING,
    FINISHED
}

@Composable
fun CleanupRushScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    sfxManager: SfxManager?,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(CleanupRushGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var spots by remember { mutableStateOf<List<AlgaeSpot>>(emptyList()) }
    var gameAreaWidth by remember { mutableStateOf(0f) }
    var gameAreaHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == CleanupRushGameState.PLAYING
    )
    
    // Spawn algae spots - difficulty affects spawn rate, max spots, and spot size
    LaunchedEffect(gameState, gameAreaWidth, difficulty) {
        if (gameState == CleanupRushGameState.PLAYING && gameAreaWidth > 0f) {
            spots = emptyList()
            score = 0
            val maxSpots = when (difficulty) {
                MiniGameDifficulty.EASY -> 10
                MiniGameDifficulty.MEDIUM -> 12
                MiniGameDifficulty.HARD -> 15
            }
            val baseSpawnDelay = (2000L / difficulty.multiplier).toLong()
            var spawnDelay = baseSpawnDelay
            while (gameState == CleanupRushGameState.PLAYING) {
                delay(spawnDelay)
                if (gameState == CleanupRushGameState.PLAYING && spots.size < maxSpots && gameAreaWidth > 0f) {
                    val baseSize = with(density) { (20.dp / difficulty.multiplier).toPx() }
                    val sizeVariation = with(density) { (30.dp / difficulty.multiplier).toPx() }
                    spots = spots + AlgaeSpot(
                        id = java.util.UUID.randomUUID().toString(),
                        x = Random.nextFloat() * gameAreaWidth,
                        y = Random.nextFloat() * gameAreaHeight,
                        size = Random.nextFloat() * sizeVariation + baseSize
                    )
                    // Gradually increase spawn rate (decrease delay)
                    spawnDelay = (spawnDelay * 0.95f).toLong().coerceAtLeast((500L / difficulty.multiplier).toLong())
                }
            }
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if (gameState == CleanupRushGameState.PLAYING && timeRemaining == 0 && !isTimerActive) {
            gameState = CleanupRushGameState.FINISHED
        }
    }
    
    // Calculate result when game finishes - apply difficulty multiplier to rewards
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == CleanupRushGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.CLEANUP_RUSH)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.CLEANUP_RUSH, score)
            }
            
            MiniGameResult(
                type = MiniGameType.CLEANUP_RUSH,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameState) {
                CleanupRushGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "🧹",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Cleanup Rush",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
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
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.CLEANUP_RUSH)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Tap algae spots to clean the tank!",
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
                            gameState = CleanupRushGameState.PLAYING
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
                
                CleanupRushGameState.PLAYING -> {
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
                    
                    // Game area with tank-like background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE0F2F1),
                                        Color(0xFFB2DFDB),
                                        Color(0xFF80CBC4)
                                    )
                                )
                            )
                            .onSizeChanged { size ->
                                gameAreaWidth = size.width.toFloat()
                                gameAreaHeight = size.height.toFloat()
                            }
                            .pointerInput(gameState) {
                                detectTapGestures { tapOffset ->
                                    if (gameState == CleanupRushGameState.PLAYING) {
                                        // Check if tap hit any spot
                                        spots = spots.filter { spot ->
                                            val distance = sqrt(
                                                (tapOffset.x - spot.x).pow(2) + (tapOffset.y - spot.y).pow(2)
                                            )
                                            if (distance <= spot.size) {
                                                // Spot cleaned!
                                                score++
                                                // Play clean splash sound
                                                sfxManager?.play(SfxEvent.CLEAN_SPLASH)
                                                false // Remove spot
                                            } else {
                                                true // Keep spot
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        spots.forEach { spot ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(
                                        x = with(density) { spot.x.dp },
                                        y = with(density) { spot.y.dp }
                                    )
                                    .size(with(density) { spot.size.dp })
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF66BB6A),
                                                Color(0xFF4CAF50),
                                                Color(0xFF388E3C)
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(
                                                spot.size / 2f,
                                                spot.size / 2f
                                            ),
                                            radius = spot.size / 2f
                                        )
                                    )
                            ) {
                                // Add a subtle inner circle for depth
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(with(density) { 4.dp })
                                        .clip(CircleShape)
                                        .background(
                                            Color(0xFF2E7D32).copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
                
                CleanupRushGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = CleanupRushGameState.WAITING
                                score = 0
                                spots = emptyList()
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
