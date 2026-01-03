package com.charles.virtualpet.fishtank.ui.minigame.bubblepop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class Bubble(
    val id: String,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float
)

enum class BubblePopGameState {
    WAITING,
    PLAYING,
    FINISHED
}

@Composable
fun BubblePopScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(BubblePopGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var bubbles by remember { mutableStateOf<List<Bubble>>(emptyList()) }
    var gameAreaWidth by remember { mutableStateOf(0f) }
    var gameAreaHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == BubblePopGameState.PLAYING
    )
    
    // Spawn bubbles - difficulty affects spawn rate and bubble properties
    LaunchedEffect(gameState, gameAreaWidth, difficulty) {
        if (gameState == BubblePopGameState.PLAYING && gameAreaWidth > 0f) {
            bubbles = emptyList()
            score = 0
            val baseSpawnMin = (1000 / difficulty.multiplier).toLong()
            val baseSpawnMax = (3000 / difficulty.multiplier).toLong()
            while (gameState == BubblePopGameState.PLAYING) {
                delay(Random.nextLong(baseSpawnMin, baseSpawnMax))
                if (gameState == BubblePopGameState.PLAYING && gameAreaWidth > 0f) {
                    val baseRadius = with(density) { (15.dp / difficulty.multiplier).toPx() }
                    val radiusVariation = with(density) { (20.dp / difficulty.multiplier).toPx() }
                    bubbles = bubbles + Bubble(
                        id = java.util.UUID.randomUUID().toString(),
                        x = Random.nextFloat() * gameAreaWidth,
                        y = gameAreaHeight, // Start at bottom
                        radius = Random.nextFloat() * radiusVariation + baseRadius,
                        speed = (Random.nextFloat() * 2f + 1f) * difficulty.multiplier // Faster on harder difficulties
                    )
                }
            }
        }
    }
    
    // Move bubbles
    LaunchedEffect(gameState) {
        if (gameState == BubblePopGameState.PLAYING) {
            while (gameState == BubblePopGameState.PLAYING) {
                delay(16) // ~60fps
                bubbles = bubbles.map { bubble ->
                    bubble.copy(y = bubble.y - bubble.speed)
                }.filter { it.y > -it.radius } // Remove bubbles that went off screen
            }
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if (gameState == BubblePopGameState.PLAYING && timeRemaining == 0 && !isTimerActive) {
            gameState = BubblePopGameState.FINISHED
        }
    }
    
    // Calculate result when game finishes - apply difficulty multiplier to rewards
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == BubblePopGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.BUBBLE_POP)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.BUBBLE_POP, score)
            }
            
            MiniGameResult(
                type = MiniGameType.BUBBLE_POP,
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
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9)
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
                BubblePopGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "🫧",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Bubble Pop",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
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
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.BUBBLE_POP)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Tap bubbles as they float upward!",
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
                            gameState = BubblePopGameState.PLAYING
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
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
                
                BubblePopGameState.PLAYING -> {
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
                                    color = if (timeRemaining <= 5) Color.Red else Color(0xFF1976D2)
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
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                    
                    // Game area with gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE0F7FA),
                                        Color(0xFFB2EBF2),
                                        Color(0xFF80DEEA)
                                    )
                                )
                            )
                            .pointerInput(gameState) {
                                detectTapGestures { tapOffset ->
                                    if (gameState == BubblePopGameState.PLAYING) {
                                        // Update game area dimensions
                                        gameAreaWidth = size.width.toFloat()
                                        gameAreaHeight = size.height.toFloat()
                                        
                                        // Check if tap hit any bubble
                                        bubbles = bubbles.filter { bubble ->
                                            val distance = sqrt(
                                                (tapOffset.x - bubble.x).pow(2) + (tapOffset.y - bubble.y).pow(2)
                                            )
                                            if (distance <= bubble.radius) {
                                                // Bubble popped!
                                                score++
                                                false // Remove bubble
                                            } else {
                                                true // Keep bubble
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Update game area dimensions
                            gameAreaWidth = size.width
                            gameAreaHeight = size.height
                            
                            bubbles.forEach { bubble ->
                                // Draw bubble with gradient effect
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFE1F5FE).copy(alpha = 0.9f),
                                            Color(0xFF64B5F6).copy(alpha = 0.7f)
                                        ),
                                        center = Offset(bubble.x, bubble.y),
                                        radius = bubble.radius
                                    ),
                                    radius = bubble.radius,
                                    center = Offset(bubble.x, bubble.y)
                                )
                                // Draw outline
                                drawCircle(
                                    color = Color(0xFF1976D2),
                                    radius = bubble.radius,
                                    center = Offset(bubble.x, bubble.y),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                                // Draw highlight
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.6f),
                                    radius = bubble.radius * 0.4f,
                                    center = Offset(bubble.x - bubble.radius * 0.3f, bubble.y - bubble.radius * 0.3f)
                                )
                            }
                        }
                    }
                }
                
                BubblePopGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = BubblePopGameState.WAITING
                                score = 0
                                bubbles = emptyList()
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
