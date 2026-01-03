package com.charles.virtualpet.fishtank.ui.minigame.fooddrop

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt

data class Pellet(
    val id: String,
    val x: Float, // Normalized 0-1
    var y: Float, // Normalized 0-1, starts at 0
    val startTime: Long = System.currentTimeMillis()
)

enum class FoodDropGameState {
    WAITING,
    PLAYING,
    FINISHED
}

@Composable
fun FoodDropScreen(
    highScoreStore: com.charles.virtualpet.fishtank.ui.minigame.HighScoreStore,
    onFinish: (MiniGameResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(FoodDropGameState.WAITING) }
    var difficulty by remember { mutableStateOf(MiniGameDifficulty.MEDIUM) }
    var score by remember { mutableIntStateOf(0) }
    var feederX by remember { mutableFloatStateOf(0.5f) } // Normalized 0-1
    var pellets by remember { mutableStateOf<List<Pellet>>(emptyList()) }
    var fishX by remember { mutableFloatStateOf(0.5f) } // Normalized 0-1, fish position
    val fishY = 0.8f // Fish stays at bottom (normalized)
    var gameAreaWidth by remember { mutableStateOf(0f) }
    var gameAreaHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    // Game timer
    val (timeRemaining, isTimerActive) = useGameTimer(
        durationSeconds = 30,
        isActive = gameState == FoodDropGameState.PLAYING
    )
    
    // Move pellets down
    LaunchedEffect(gameState, pellets.size) {
        if (gameState == FoodDropGameState.PLAYING) {
            while (gameState == FoodDropGameState.PLAYING) {
                delay(16) // ~60fps
                pellets = pellets.map { pellet ->
                    val elapsed = System.currentTimeMillis() - pellet.startTime
                    val fallDuration = 3000L // 3 seconds to fall
                    val progress = (elapsed.toFloat() / fallDuration).coerceIn(0f, 1f)
                    pellet.copy(y = progress)
                }.filter { it.y < 1.0f } // Remove pellets that reached bottom
            }
        }
    }
    
    // Move fish toward pellet X (lerp)
    LaunchedEffect(gameState, pellets.size) {
        if (gameState == FoodDropGameState.PLAYING && pellets.isNotEmpty()) {
            while (gameState == FoodDropGameState.PLAYING) {
                delay(16) // ~60fps
                val activePellet = pellets.firstOrNull()
                if (activePellet != null) {
                    // Lerp fish toward pellet X
                    val targetX = activePellet.x
                    val lerpSpeed = 0.05f // Smooth movement
                    fishX += (targetX - fishX) * lerpSpeed
                    fishX = fishX.coerceIn(0.1f, 0.9f) // Keep fish in bounds
                }
            }
        }
    }
    
    // Collision detection
    LaunchedEffect(gameState, pellets.size) {
        if (gameState == FoodDropGameState.PLAYING) {
            while (gameState == FoodDropGameState.PLAYING) {
                delay(16) // ~60fps
                val pelletsToRemove = mutableListOf<String>()
                
                pellets.forEach { pellet ->
                    // Check if pellet is near fish Y position
                    if (pellet.y >= fishY - 0.1f && pellet.y <= fishY + 0.1f) {
                        val dx = pellet.x - fishX
                        val distance = abs(dx)
                        
                        // Collision threshold
                        if (distance < 0.15f) {
                            pelletsToRemove.add(pellet.id)
                            
                            // Score: +5 per catch
                            var points = 5
                            
                            // Bonus if caught near center (x: 0.4-0.6)
                            if (pellet.x >= 0.4f && pellet.x <= 0.6f) {
                                points += 2
                            }
                            
                            score += points
                        }
                    }
                }
                
                // Remove caught pellets
                if (pelletsToRemove.isNotEmpty()) {
                    pellets = pellets.filter { it.id !in pelletsToRemove }
                }
            }
        }
    }
    
    // Check timer end
    LaunchedEffect(timeRemaining, isTimerActive) {
        if (gameState == FoodDropGameState.PLAYING && timeRemaining == 0 && !isTimerActive) {
            gameState = FoodDropGameState.FINISHED
        }
    }
    
    // Calculate result when game finishes
    val gameResult = remember(gameState, score, difficulty) {
        if (gameState == FoodDropGameState.FINISHED) {
            val (baseCoins, baseXP) = Rewards.calculateRewards(score)
            val coinsEarned = (baseCoins * difficulty.multiplier).toInt()
            val xpEarned = (baseXP * difficulty.multiplier).toInt()
            val currentHighScore = highScoreStore.get(MiniGameType.FOOD_DROP)
            val isHighScore = score > currentHighScore
            
            if (isHighScore) {
                highScoreStore.set(MiniGameType.FOOD_DROP, score)
            }
            
            MiniGameResult(
                type = MiniGameType.FOOD_DROP,
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
                        Color(0xFFFFEBEE),
                        Color(0xFFFFCDD2),
                        Color(0xFFEF9A9A)
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
                FoodDropGameState.WAITING -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Text(
                        text = "Food Drop",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935),
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
                            text = "⭐ High Score: ${highScoreStore.get(MiniGameType.FOOD_DROP)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Text(
                        text = "Move the feeder and drop food for your fish to catch!",
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
                            gameState = FoodDropGameState.PLAYING
                            score = 0
                            pellets = emptyList()
                            feederX = 0.5f
                            fishX = 0.5f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
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
                
                FoodDropGameState.PLAYING -> {
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
                                    color = if (timeRemaining <= 5) Color.Red else Color(0xFFE53935)
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
                                    color = Color(0xFFE53935)
                                )
                            }
                        }
                    }
                    
                    // Game area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE0F7FA),
                                        Color(0xFFB2EBF2),
                                        Color(0xFF80DEEA)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            gameAreaWidth = size.width
                            gameAreaHeight = size.height
                            
                            // Draw pellets
                            pellets.forEach { pellet ->
                                val pelletX = pellet.x * size.width
                                val pelletY = pellet.y * size.height
                                
                                // Draw pellet
                                drawCircle(
                                    color = Color(0xFFFF9800),
                                    radius = 12.dp.toPx(),
                                    center = Offset(pelletX, pelletY)
                                )
                                drawCircle(
                                    color = Color(0xFFFFC107),
                                    radius = 8.dp.toPx(),
                                    center = Offset(pelletX, pelletY)
                                )
                            }
                            
                            // Draw fish
                            val fishXPos = fishX * size.width
                            val fishYPos = fishY * size.height
                            
                            // Simple fish shape (circle)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF64B5F6),
                                        Color(0xFF42A5F5),
                                        Color(0xFF1976D2)
                                    ),
                                    center = Offset(fishXPos, fishYPos),
                                    radius = 30.dp.toPx()
                                ),
                                radius = 30.dp.toPx(),
                                center = Offset(fishXPos, fishYPos)
                            )
                            // Fish eye
                            drawCircle(
                                color = Color.White,
                                radius = 8.dp.toPx(),
                                center = Offset(fishXPos - 10.dp.toPx(), fishYPos - 8.dp.toPx())
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = 4.dp.toPx(),
                                center = Offset(fishXPos - 10.dp.toPx(), fishYPos - 8.dp.toPx())
                            )
                        }
                    }
                    
                    // Feeder control area
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Move Feeder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Feeder slider/drag area
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, _ ->
                                            val newX = (change.position.x / this.size.width).coerceIn(0.1f, 0.9f)
                                            feederX = newX
                                        }
                                    }
                            ) {
                                // Feeder indicator
                                Box(
                                    modifier = Modifier
                                        .offset(x = with(density) { (feederX * maxWidth.value - 20.dp.toPx()).toDp() })
                                        .size(40.dp)
                                        .background(
                                            color = Color(0xFFE53935),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🍽️",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // DROP button
                            Button(
                                onClick = {
                                    if (pellets.size < 3) { // Limit to 3 pellets at once
                                        pellets = pellets + Pellet(
                                            id = java.util.UUID.randomUUID().toString(),
                                            x = feederX,
                                            y = 0f
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "DROP",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                FoodDropGameState.FINISHED -> {
                    gameResult?.let { result ->
                        MiniGameEndScreen(
                            result = result,
                            onPlayAgain = {
                                gameState = FoodDropGameState.WAITING
                                score = 0
                                pellets = emptyList()
                                feederX = 0.5f
                                fishX = 0.5f
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

