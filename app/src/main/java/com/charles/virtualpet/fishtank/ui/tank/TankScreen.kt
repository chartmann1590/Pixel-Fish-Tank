package com.charles.virtualpet.fishtank.ui.tank

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.data.DecorationStore
import com.charles.virtualpet.fishtank.domain.MoodCalculator
import com.charles.virtualpet.fishtank.domain.model.FishMood
import com.charles.virtualpet.fishtank.ui.components.ActionButton
import com.charles.virtualpet.fishtank.ui.components.DailyTasksCard
import com.charles.virtualpet.fishtank.ui.components.FishDisplay
import com.charles.virtualpet.fishtank.ui.components.FoodItem
import com.charles.virtualpet.fishtank.ui.components.StatBar
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun TankScreen(
    viewModel: GameViewModel,
    onNavigateToMiniGame: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToPlacement: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val fishState = gameState.fishState
    val economy = gameState.economy
    val placedDecorations = gameState.tankLayout.placedDecorations

    // Calculate fish mood
    val mood = MoodCalculator.calculateMood(fishState)
    
    // Food system state
    val activeFood = remember { mutableStateListOf<FoodItem>() }
    var fishX by remember { mutableStateOf(0.5f) }
    var fishY by remember { mutableStateOf(0.5f) }
    
    // Spawn food when feed button is clicked
    fun spawnFood() {
        val foodId = UUID.randomUUID().toString()
        val randomX = kotlin.random.Random.nextFloat() * 0.6f + 0.2f // Spawn food in middle 60% of tank width
        activeFood.add(FoodItem(id = foodId, x = randomX, y = 0f))
    }
    
    // Check collisions and mark food that reaches bottom
    LaunchedEffect(activeFood.size) {
        while (true) {
            delay(16) // Check every ~16ms for smoother collision detection
            if (activeFood.isEmpty()) continue
            
            val foodToRemove = mutableListOf<String>()
            val foodToUpdate = mutableListOf<Pair<String, FoodItem>>()
            
            activeFood.forEach { food ->
                // Calculate actual food position (same as in FallingFood)
                // Use elapsed time to calculate progress for collision detection
                val elapsedTime = System.currentTimeMillis() - food.startTime
                val fallDuration = 6000L // Slower fall - 6 seconds instead of 3
                val progress = (elapsedTime.toFloat() / fallDuration).coerceIn(0f, 1f)
                val driftAmount = (kotlin.math.sin(progress * kotlin.math.PI.toFloat()) * 0.1f)
                val foodX = (food.x + driftAmount).coerceIn(0f, 1f)
                val foodY = progress
                
                // Check collision - use larger threshold and account for fish size
                // Fish is about 200dp = 0.8f normalized, food is 30dp = 0.12f normalized
                val dx = foodX - fishX
                val dy = foodY - fishY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                
                // Larger collision threshold - about 0.2f (20% of tank)
                if (distance < 0.2f) {
                    foodToRemove.add(food.id)
                    // Trigger actual feed
                    viewModel.feedFish()
                } else if (foodY >= 1.0f && !food.hasReachedBottom) {
                    // Food reached bottom - mark it but don't remove yet
                    foodToUpdate.add(Pair(food.id, food.copy(hasReachedBottom = true)))
                }
            }
            
            // Update food that reached bottom
            foodToUpdate.forEach { (id, updatedFood) ->
                val index = activeFood.indexOfFirst { it.id == id }
                if (index >= 0) {
                    activeFood[index] = updatedFood
                }
            }
            
            // Remove eaten food
            foodToRemove.forEach { id ->
                activeFood.removeAll { it.id == id }
            }
        }
    }
    
    // Function to clean uneaten food at bottom
    fun cleanUneatenFood() {
        activeFood.removeAll { it.hasReachedBottom }
    }

    // Determine tank background based on cleanliness
    val tankBackgroundRes = when {
        fishState.cleanliness >= 70f -> R.drawable.tank_clean
        else -> R.drawable.tank_dirty
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Tank background
        Image(
            painter = painterResource(id = tankBackgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title with Settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tank_title),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                androidx.compose.material3.IconButton(
                    onClick = onNavigateToSettings
                ) {
                    Text(
                        text = "⚙️",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Stats section
            StatBar(
                label = stringResource(R.string.stat_hunger),
                value = fishState.hunger,
                modifier = Modifier.fillMaxWidth()
            )
            StatBar(
                label = stringResource(R.string.stat_cleanliness),
                value = fishState.cleanliness,
                modifier = Modifier.fillMaxWidth()
            )
            StatBar(
                label = stringResource(R.string.stat_happiness),
                value = fishState.happiness,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Tasks
            DailyTasksCard(
                tasksState = gameState.dailyTasks,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mood indicator
            Text(
                text = getMoodText(mood),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = getMoodColor(mood),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fish display area with decorations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Get current food positions for fish attraction
                // Use a state to track food positions that updates continuously
                var foodPositionsState by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
                
                LaunchedEffect(activeFood.size) {
                    while (activeFood.isNotEmpty()) {
                        foodPositionsState = activeFood.map { food ->
                            val elapsedTime = System.currentTimeMillis() - food.startTime
                            val fallDuration = 6000L // Match the slower fall duration
                            val progress = if (food.hasReachedBottom) 1f else (elapsedTime.toFloat() / fallDuration).coerceIn(0f, 1f)
                            val driftAmount = (kotlin.math.sin(progress * kotlin.math.PI.toFloat()) * 0.1f)
                            val xPosition = food.x + driftAmount
                            Pair(xPosition.coerceIn(0f, 1f), progress.coerceIn(0f, 1f))
                        }
                        delay(16) // Update ~60fps
                    }
                }
                
                val foodPositions = foodPositionsState
                
                FishDisplay(
                    mood = mood,
                    onPositionUpdate = { x, y ->
                        fishX = x
                        fishY = y
                    },
                    nearbyFood = foodPositions
                )
                
                // Display falling food
                activeFood.forEach { food ->
                    FallingFood(
                        food = food,
                        containerHeight = 250.dp
                    )
                }
                
                // Display placed decorations
                placedDecorations.forEach { placed ->
                    val decoration = DecorationStore.getDecorationById(placed.decorationId)
                    if (decoration != null) {
                        val imageResId = when (decoration.drawableRes) {
                            "decoration_plant" -> R.drawable.decoration_plant
                            "decoration_rock" -> R.drawable.decoration_rock
                            "decoration_toy" -> R.drawable.decoration_toy
                            else -> R.drawable.decoration_plant
                        }
                        
                        val boxWidth = 250.dp
                        val decorationSize = 40.dp
                        
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = decoration.name,
                            modifier = Modifier
                                .size(decorationSize)
                                .offset(
                                    x = (placed.x * boxWidth.value).dp - decorationSize / 2,
                                    y = (placed.y * boxWidth.value).dp - decorationSize / 2
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = stringResource(R.string.button_feed),
                    iconRes = R.drawable.ic_feed,
                    onClick = { spawnFood() },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton(
                    text = stringResource(R.string.button_clean),
                    iconRes = R.drawable.ic_clean,
                    onClick = { 
                        viewModel.cleanTank()
                        cleanUneatenFood() // Remove uneaten food at bottom
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini-game button
            ActionButton(
                text = "🎮 Mini-Game",
                iconRes = null,
                onClick = onNavigateToMiniGame,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Decoration buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = "🛍️ Store",
                    iconRes = null,
                    onClick = onNavigateToStore,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ActionButton(
                    text = "🎨 Decorate",
                    iconRes = null,
                    onClick = onNavigateToPlacement,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_level),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${fishState.level}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_xp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${fishState.xp}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_coins),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${economy.coins}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun getMoodText(mood: FishMood): String {
    return when (mood) {
        FishMood.HAPPY -> "😊 Happy"
        FishMood.NEUTRAL -> "😐 Neutral"
        FishMood.HUNGRY -> "😋 Hungry"
        FishMood.DIRTY -> "😤 Dirty Tank"
        FishMood.SAD -> "😢 Sad"
    }
}

@Composable
private fun getMoodColor(mood: FishMood): androidx.compose.ui.graphics.Color {
    return when (mood) {
        FishMood.HAPPY -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        FishMood.NEUTRAL -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
        FishMood.HUNGRY -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        FishMood.DIRTY -> androidx.compose.ui.graphics.Color(0xFF795548) // Brown
        FishMood.SAD -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    }
}

@Composable
private fun FallingFood(
    food: FoodItem,
    containerHeight: androidx.compose.ui.unit.Dp
) {
    // Trigger animation when food is created
    var startAnimation by remember(food.id) { mutableStateOf(false) }
    
    LaunchedEffect(food.id) {
        startAnimation = true
    }
    
    // Animate from 0 (top) to 1 (bottom) over 6 seconds (slower fall)
    val targetProgress = if (food.hasReachedBottom) 1f else if (startAnimation) 1f else 0f
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 6000, // Slower - 6 seconds instead of 3
            easing = LinearEasing
        ),
        label = "food_fall"
    )
    
    // Slight horizontal drift for more natural movement
    val driftAmount = (kotlin.math.sin(progress * kotlin.math.PI.toFloat()) * 0.1f)
    val xPosition = food.x + driftAmount
    
    // Use BoxWithConstraints to get actual container width
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val containerWidthDp = maxWidth
        
        Image(
            painter = painterResource(id = R.drawable.food_normal),
            contentDescription = "Food",
            modifier = Modifier
                .size(30.dp)
                .offset(
                    x = (xPosition * containerWidthDp.value - 15).dp,
                    y = (progress * containerHeight.value).dp
                )
        )
    }
}

