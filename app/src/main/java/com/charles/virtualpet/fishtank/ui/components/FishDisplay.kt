package com.charles.virtualpet.fishtank.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.model.FishMood
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FishDisplay(
    mood: FishMood = FishMood.NEUTRAL,
    modifier: Modifier = Modifier,
    onPositionUpdate: ((Float, Float) -> Unit)? = null,
    nearbyFood: List<Pair<Float, Float>> = emptyList(), // List of (x, y) food positions normalized 0-1
    onClick: (() -> Unit)? = null // Callback when fish is clicked
) {
    val fishImageRes = when (mood) {
        FishMood.HAPPY -> R.drawable.fish_happy
        FishMood.HUNGRY -> R.drawable.fish_sad
        FishMood.DIRTY -> R.drawable.fish_dirty
        FishMood.SAD -> R.drawable.fish_sad
        FishMood.NEUTRAL -> R.drawable.fish_starter
    }

    val contentDescription = when (mood) {
        FishMood.HAPPY -> "Happy Fish"
        FishMood.HUNGRY -> "Hungry Fish"
        FishMood.DIRTY -> "Fish in Dirty Tank"
        FishMood.SAD -> "Sad Fish"
        FishMood.NEUTRAL -> "Fish"
    }

    // Animation for fish movement - creates a smooth swimming pattern
    val infiniteTransition = rememberInfiniteTransition(label = "fish_movement")
    
    // Horizontal movement (swimming left to right)
    val horizontalOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "horizontal_movement"
    )
    
    // Vertical movement (swimming up and down)
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical_movement"
    )
    
    // Circular movement for more natural swimming
    val circularPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "circular_movement"
    )
    
    // Base swimming pattern
    val baseX = horizontalOffset + (cos(Math.toRadians(circularPhase.toDouble())) * 30).toFloat()
    val baseY = verticalOffset + (sin(Math.toRadians(circularPhase.toDouble())) * 30).toFloat()
    
    // Current fish position in normalized coordinates (0-1)
    val currentFishX = (0.5f + baseX / 250f).coerceIn(0f, 1f)
    val currentFishY = (0.5f + baseY / 250f).coerceIn(0f, 1f)
    
    // Find nearest food and calculate attraction
    var foodAttractionX = 0f
    var foodAttractionY = 0f
    var hasNearbyFood = false
    
    if (nearbyFood.isNotEmpty()) {
        var nearestDistance = Float.MAX_VALUE
        var nearestFood: Pair<Float, Float>? = null
        
        nearbyFood.forEach { (foodX, foodY) ->
            val dx = foodX - currentFishX
            val dy = foodY - currentFishY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            
            if (distance < nearestDistance && distance < 0.5f) { // Only attract if food is within 50% of tank
                nearestDistance = distance
                nearestFood = Pair(foodX, foodY)
            }
        }
        
        if (nearestFood != null) {
            hasNearbyFood = true
            val (targetX, targetY) = nearestFood!!
            val dx = targetX - currentFishX
            val dy = targetY - currentFishY
            
            // Normalize direction and apply attraction strength (stronger when closer)
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            if (distance > 0.01f) {
                val attractionStrength = (1f - distance / 0.5f).coerceIn(0f, 1f) * 150f // Max 150dp attraction
                foodAttractionX = (dx / distance) * attractionStrength
                foodAttractionY = (dy / distance) * attractionStrength
            }
        }
    }
    
    // Blend base movement with food attraction
    // When food is nearby, gradually move towards it while maintaining some natural movement
    val blendFactor = if (hasNearbyFood) 0.6f else 0f // 60% towards food, 40% natural movement when food nearby
    val targetX = baseX * (1f - blendFactor) + foodAttractionX * blendFactor
    val targetY = baseY * (1f - blendFactor) + foodAttractionY * blendFactor
    
    // Use animateFloatAsState for smooth movement that follows continuously changing targets
    val smoothX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "smooth_x"
    )
    
    val smoothY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "smooth_y"
    )
    
    // Track previous X position and direction for image flipping
    val previousX = remember { mutableStateOf(smoothX) }
    val isFacingRightState = remember { mutableStateOf(true) }
    
    // Determine direction for image flipping based on movement velocity
    // Calculate direction change and update facing direction
    LaunchedEffect(smoothX) {
        val directionChange = smoothX - previousX.value
        // Use a small threshold to detect direction changes, but prevent rapid flipping
        val flipThreshold = 1f // Flip if moved at least 1dp
        
        if (kotlin.math.abs(directionChange) > flipThreshold) {
            val newDirection = directionChange > 0 // true = moving right, false = moving left
            // Only update if direction actually changed
            if (newDirection != isFacingRightState.value) {
                isFacingRightState.value = newDirection
            }
        }
        previousX.value = smoothX
    }
    
    // Use animated state for smooth flipping
    val scaleX by animateFloatAsState(
        targetValue = if (isFacingRightState.value) 1f else -1f,
        animationSpec = tween(durationMillis = 200),
        label = "flip_animation"
    )
    
    // Notify parent of fish position (normalized to 0.0-1.0 range)
    val normalizedX = (0.5f + smoothX / 250f).coerceIn(0f, 1f)
    val normalizedY = (0.5f + smoothY / 250f).coerceIn(0f, 1f)
    
    LaunchedEffect(normalizedX, normalizedY) {
        onPositionUpdate?.invoke(normalizedX, normalizedY)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = fishImageRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(200.dp)
                .scale(scaleX = scaleX, scaleY = 1f)
                .offset(x = smoothX.dp, y = smoothY.dp)
                .then(
                    if (onClick != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                onClick()
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        )
    }
}

