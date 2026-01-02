package com.charles.virtualpet.fishtank.ui.tank

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.data.DecorationStore
import com.charles.virtualpet.fishtank.domain.model.PlacedDecoration
import com.charles.virtualpet.fishtank.ui.components.FishDisplay
import com.charles.virtualpet.fishtank.ui.components.FoodItem
import com.charles.virtualpet.fishtank.domain.model.FishMood

@Composable
fun TankPlayableArea(
    placedDecorations: List<PlacedDecoration>,
    mood: FishMood,
    activeFood: List<FoodItem>,
    coinRewards: List<CoinReward>,
    containerSize: IntSize?,
    screenSize: IntSize?,
    containerScreenPosition: androidx.compose.ui.geometry.Offset?,
    fishX: Float,
    fishY: Float,
    foodPositions: List<Pair<Float, Float>>,
    onFishClick: () -> Unit,
    onPositionUpdate: (Float, Float) -> Unit,
    onRemoveDecoration: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier.fillMaxSize()) {
        // Tank background removed for screenshot capture - only fish and decorations should be visible
        // Background is rendered at root level in TankScreen for display purposes
        
        // Display placed decorations - convert from screen-relative to container-relative coordinates
        if (containerSize != null && screenSize != null && containerScreenPosition != null) {
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
                    
                    // Convert from screen-normalized (0-1) to container-relative pixels
                    // Decoration is placed at (placed.x, placed.y) normalized to screen
                    // Screen pixel position: (placed.x * screenWidth, placed.y * screenHeight)
                    // Container-relative position: (screenPixelX - containerX, screenPixelY - containerY)
                    val screenWidthPx = screenSize.width.toFloat()
                    val screenHeightPx = screenSize.height.toFloat()
                    val screenPixelX = placed.x * screenWidthPx
                    val screenPixelY = placed.y * screenHeightPx
                    
                    val containerX = containerScreenPosition.x
                    val containerY = containerScreenPosition.y
                    val containerRelativeX = screenPixelX - containerX
                    val containerRelativeY = screenPixelY - containerY
                    
                    // Only render if decoration is within container bounds
                    val decorationSizePx = with(density) { decorationSize.toPx() }
                    if (containerRelativeX >= -decorationSizePx / 2 && 
                        containerRelativeX <= containerSize.width + decorationSizePx / 2 &&
                        containerRelativeY >= -decorationSizePx / 2 && 
                        containerRelativeY <= containerSize.height + decorationSizePx / 2) {
                        
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = decoration.name,
                            modifier = Modifier
                                .size(decorationSize)
                                .offset(
                                    x = with(density) { (containerRelativeX - decorationSize.toPx() / 2).toDp() },
                                    y = with(density) { (containerRelativeY - decorationSize.toPx() / 2).toDp() }
                                )
                                .clickable {
                                    onRemoveDecoration(placed.id)
                                }
                        )
                    }
                }
            }
        }
        
        // Fish display
        FishDisplay(
            mood = mood,
            onPositionUpdate = onPositionUpdate,
            nearbyFood = foodPositions,
            onClick = onFishClick
        )
        
        // Display falling food
        val containerHeightDp = containerSize?.let {
            with(density) { it.height.toDp() }
        } ?: TankDimensions.TANK_HEIGHT
        
        activeFood.forEach { food ->
            FallingFood(
                food = food,
                containerHeight = containerHeightDp
            )
        }
        
        // Display coin rewards
        if (containerSize != null && coinRewards.isNotEmpty()) {
            coinRewards.forEach { reward ->
                CoinRewardDisplay(
                    reward = reward,
                    containerSize = containerSize,
                    density = density
                )
            }
        }
        
        // Note: Floating bubbles are rendered at root level in TankScreen due to tap detection requirements
    }
}

