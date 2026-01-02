package com.charles.virtualpet.fishtank.ui.minigame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DifficultySelector(
    selectedDifficulty: MiniGameDifficulty,
    onDifficultySelected: (MiniGameDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            MiniGameDifficulty.values().forEach { difficulty ->
                DifficultyButton(
                    difficulty = difficulty,
                    isSelected = difficulty == selectedDifficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyButton(
    difficulty: MiniGameDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = when (difficulty) {
        MiniGameDifficulty.EASY -> listOf(
            Color(0xFF81C784),
            Color(0xFF66BB6A),
            Color(0xFF4CAF50)
        )
        MiniGameDifficulty.MEDIUM -> listOf(
            Color(0xFFFFB74D),
            Color(0xFFFFA726),
            Color(0xFFFF9800)
        )
        MiniGameDifficulty.HARD -> listOf(
            Color(0xFFE57373),
            Color(0xFFEF5350),
            Color(0xFFE53935)
        )
    }
    
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(colors)
                } else {
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = when (difficulty) {
                    MiniGameDifficulty.EASY -> "😊"
                    MiniGameDifficulty.MEDIUM -> "😐"
                    MiniGameDifficulty.HARD -> "🔥"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = difficulty.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Text(
                    text = "${(difficulty.multiplier * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

