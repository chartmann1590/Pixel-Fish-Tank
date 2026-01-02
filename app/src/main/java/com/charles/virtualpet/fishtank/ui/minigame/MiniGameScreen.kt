package com.charles.virtualpet.fishtank.ui.minigame

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel

enum class GameState {
    WAITING,    // Waiting to start
    PLAYING,    // Game in progress
    FINISHED    // Game completed
}

@Composable
fun MiniGameScreen(
    viewModel: GameViewModel,
    repository: GameStateRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(GameState.WAITING) }
    var score by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableIntStateOf(30) } // 30 seconds
    var highScore by remember { mutableIntStateOf(0) }
    var coinsEarned by remember { mutableIntStateOf(0) }
    var xpEarned by remember { mutableIntStateOf(0) }
    
    // Load high score
    LaunchedEffect(Unit) {
        highScore = repository.highScore.first()
    }

    // Game timer
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            while (timeRemaining > 0 && gameState == GameState.PLAYING) {
                delay(1000)
                timeRemaining--
            }
            if (timeRemaining == 0) {
                gameState = GameState.FINISHED
                // Calculate rewards
                coinsEarned = score * 2 // 2 coins per point
                xpEarned = score / 5 // 1 XP per 5 points
                
                // Save high score and update local if it's a new record
                repository.saveHighScore(score)
                if (score > highScore) {
                    highScore = score
                }
                
                // Update game state with rewards
                viewModel.addCoins(coinsEarned)
                viewModel.addXP(xpEarned)
                
                // Complete mini-game task
                viewModel.completeMinigameTask()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Tap the Fish!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "High Score: $highScore",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (gameState) {
            GameState.WAITING -> {
                Text(
                    text = "Tap the fish as many times as you can in 30 seconds!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
                
                Button(
                    onClick = {
                        gameState = GameState.PLAYING
                        score = 0
                        timeRemaining = 30
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Start Game")
                }
            }
            
            GameState.PLAYING -> {
                // Timer
                Text(
                    text = "Time: $timeRemaining",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timeRemaining <= 5) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Score
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Tappable fish
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(100),
                    label = "fish_scale"
                )
                
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .clickable {
                            score++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐟",
                        fontSize = 80.sp
                    )
                }
                
                Text(
                    text = "Tap the fish!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            GameState.FINISHED -> {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Text(
                    text = "Final Score: $score",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (score > highScore) {
                    Text(
                        text = "🎉 New High Score! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Text(
                    text = "Coins Earned: +$coinsEarned",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Text(
                    text = "XP Earned: +$xpEarned",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        gameState = GameState.WAITING
                        score = 0
                        timeRemaining = 30
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Play Again")
                }
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Back to Tank")
                }
            }
        }
    }
}

