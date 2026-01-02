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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.data.DecorationStore
import com.charles.virtualpet.fishtank.domain.MoodCalculator
import com.charles.virtualpet.fishtank.domain.model.FishMood
import com.charles.virtualpet.fishtank.ui.components.ActionButton
import com.charles.virtualpet.fishtank.ui.components.DailyTasksCard
import com.charles.virtualpet.fishtank.ui.components.ExpandableFAB
import com.charles.virtualpet.fishtank.ui.components.FABAction
import com.charles.virtualpet.fishtank.ui.components.FishDisplay
import com.charles.virtualpet.fishtank.ui.components.FoodItem
import com.charles.virtualpet.fishtank.ui.components.StatBar
import com.charles.virtualpet.fishtank.ui.tank.TankDimensions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Button
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
    val ownedDecorations = gameState.economy.inventoryItems
        .filter { it.type == com.charles.virtualpet.fishtank.domain.model.ItemType.DECORATION && it.quantity > 0 }

    // Calculate fish mood
    val mood = MoodCalculator.calculateMood(fishState)
    
    // Decoration placement mode
    var isPlacingDecoration by remember { mutableStateOf(false) }
    var selectedDecorationId by remember { mutableStateOf<String?>(null) }
    var screenSize by remember { mutableStateOf<IntSize?>(null) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    
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
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = size
            }
    ) {
        // Tank background
        Image(
            painter = painterResource(id = tankBackgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Full screen tap detection for decoration placement
        if (isPlacingDecoration && selectedDecorationId != null && screenSize != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenSize, selectedDecorationId, economy.inventoryItems) {
                        detectTapGestures { tapOffset ->
                            // Double-check quantity before placing
                            val inventoryItem = economy.inventoryItems.find { it.id == selectedDecorationId }
                            if (inventoryItem != null && inventoryItem.quantity > 0) {
                                // Use FULL SCREEN size for coordinates
                                val x = (tapOffset.x / screenSize!!.width.toFloat()).coerceIn(0f, 1f)
                                val y = (tapOffset.y / screenSize!!.height.toFloat()).coerceIn(0f, 1f)
                                viewModel.placeDecoration(selectedDecorationId!!, x, y)
                                if (placedDecorations.isEmpty()) {
                                    viewModel.completeDecorateTask()
                                }
                            }
                            selectedDecorationId = null
                            isPlacingDecoration = false
                        }
                    }
            )
        }

        // Display placed decorations at ROOT LEVEL - NO BOUNDARIES, ANYWHERE ON SCREEN
        placedDecorations.forEach { placed ->
            val decoration = DecorationStore.getDecorationById(placed.decorationId)
            if (decoration != null) {
                val imageResId = when (decoration.drawableRes) {
                    "decoration_plant" -> R.drawable.decoration_plant
                    "decoration_rock" -> R.drawable.decoration_rock
                    "decoration_toy" -> R.drawable.decoration_toy
                    else -> R.drawable.decoration_plant
                }
                
                val decorationSize = 240.dp
                
                // Use FULL SCREEN size - coordinates are relative to entire screen
                val screenWidthPx = screenSize?.width?.toFloat() ?: with(density) { 400.dp.toPx() }
                val screenHeightPx = screenSize?.height?.toFloat() ?: with(density) { 800.dp.toPx() }
                val xPos = placed.x * screenWidthPx
                val yPos = placed.y * screenHeightPx
                
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = decoration.name,
                    modifier = Modifier
                        .size(decorationSize)
                        .offset(
                            x = with(density) { (xPos - decorationSize.toPx() / 2).toDp() },
                            y = with(density) { (yPos - decorationSize.toPx() / 2).toDp() }
                        )
                        .clickable {
                            // Remove decoration when tapped
                            viewModel.removeDecoration(placed.id)
                        }
                )
            }
        }

        // Expandable FAB for actions
        ExpandableFAB(
            actions = listOf(
                FABAction(
                    label = stringResource(R.string.button_feed),
                    iconRes = R.drawable.ic_feed,
                    onClick = { spawnFood() }
                ),
                FABAction(
                    label = stringResource(R.string.button_clean),
                    iconRes = R.drawable.ic_clean,
                    onClick = { 
                        viewModel.cleanTank()
                        cleanUneatenFood() // Remove uneaten food at bottom
                    }
                ),
                FABAction(
                    label = "Mini-Game",
                    iconRes = null,
                    onClick = onNavigateToMiniGame
                ),
                FABAction(
                    label = "Store",
                    iconRes = null,
                    onClick = onNavigateToStore
                ),
                FABAction(
                    label = "Decorate",
                    iconRes = null,
                    onClick = {
                        if (ownedDecorations.isNotEmpty()) {
                            isPlacingDecoration = true
                            selectedDecorationId = ownedDecorations.first().id
                        } else {
                            onNavigateToStore()
                        }
                    }
                )
            ),
            modifier = Modifier.fillMaxSize()
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

            // Fish display area with decorations - Expanded to fill space down to bottom stats bar
            var containerSize by remember { mutableStateOf<IntSize?>(null) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { size ->
                        containerSize = size
                    }
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
                // Use actual container height, or fallback to 250.dp if not measured yet
                val containerHeightDp = containerSize?.let { 
                    with(density) { it.height.toDp() }
                } ?: TankDimensions.TANK_HEIGHT
                
                activeFood.forEach { food ->
                    FallingFood(
                        food = food,
                        containerHeight = containerHeightDp
                    )
                }
                
                // Show decoration selector when in placement mode
                if (isPlacingDecoration && ownedDecorations.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Tap on tank to place decoration",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                            ) {
                                items(ownedDecorations) { item ->
                                    val decoration = DecorationStore.getDecorationById(item.id)
                                    if (decoration != null) {
                                        // Get the latest quantity from the current game state
                                        val currentItem = gameState.economy.inventoryItems.find { it.id == item.id }
                                        val availableCount = currentItem?.quantity ?: 0
                                        
                                        // Only show if we actually have quantity available
                                        if (availableCount <= 0) {
                                            return@items
                                        }
                                        
                                        val imageResId = when (decoration.drawableRes) {
                                            "decoration_plant" -> R.drawable.decoration_plant
                                            "decoration_rock" -> R.drawable.decoration_rock
                                            "decoration_toy" -> R.drawable.decoration_toy
                                            else -> R.drawable.decoration_plant
                                        }
                                        
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clickable { 
                                                        if (availableCount > 0) {
                                                            selectedDecorationId = decoration.id
                                                        }
                                                    }
                                            ) {
                                                Box {
                                                    Image(
                                                        painter = painterResource(id = imageResId),
                                                        contentDescription = decoration.name,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    if (availableCount <= 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${availableCount} available",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            androidx.compose.material3.Button(
                                onClick = { isPlacingDecoration = false },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
                
            }

        }

        // Bottom stats bar - positioned at absolute bottom, leaving space for FAB
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 80.dp, bottom = 16.dp) // Leave space for FAB on right
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.label_level),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

