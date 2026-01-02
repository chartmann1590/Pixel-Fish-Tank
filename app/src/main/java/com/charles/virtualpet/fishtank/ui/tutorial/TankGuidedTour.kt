package com.charles.virtualpet.fishtank.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity

data class TourStep(
    val targetId: String,
    val instruction: String
)

object TankTourSteps {
    val steps = listOf(
        TourStep(
            targetId = "feed",
            instruction = "Tap here to feed your fish and keep it healthy!"
        ),
        TourStep(
            targetId = "clean",
            instruction = "Keep the tank clean for a happy fish!"
        ),
        TourStep(
            targetId = "minigame",
            instruction = "Play mini-games to earn coins and XP!"
        ),
        TourStep(
            targetId = "decorate",
            instruction = "Decorate your tank to boost happiness!"
        )
    )
}

@Composable
fun TankGuidedTour(
    showTour: Boolean,
    buttonBounds: Map<String, Rect>,
    onTourComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showTour) return
    
    val steps = TankTourSteps.steps
    var currentStepIndex by remember { mutableIntStateOf(0) }
    
    if (currentStepIndex >= steps.size) {
        onTourComplete()
        return
    }
    
    val currentStep = steps[currentStepIndex]
    val targetBounds = buttonBounds[currentStep.targetId]
    val isLastStep = currentStepIndex == steps.size - 1
    
    GuidedOverlay(
        targetBounds = targetBounds,
        tooltipText = currentStep.instruction,
        isLastStep = isLastStep,
        onNext = {
            if (currentStepIndex < steps.size - 1) {
                currentStepIndex++
            } else {
                onTourComplete()
            }
        },
        onDone = {
            onTourComplete()
        },
        modifier = modifier
    )
}

// Helper composable to capture button bounds
@Composable
fun CaptureButtonBounds(
    buttonId: String,
    onBoundsCaptured: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val topLeft = coordinates.localToRoot(Offset.Zero)
            val bottomRight = coordinates.localToRoot(
                Offset(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            )
            val rect = Rect(
                left = topLeft.x,
                top = topLeft.y,
                right = bottomRight.x,
                bottom = bottomRight.y
            )
            onBoundsCaptured(buttonId, rect)
        }
    )
}

