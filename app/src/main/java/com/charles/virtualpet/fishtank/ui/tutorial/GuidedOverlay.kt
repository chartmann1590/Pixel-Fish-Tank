package com.charles.virtualpet.fishtank.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuidedOverlay(
    targetBounds: Rect?,
    tooltipText: String,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    var screenSize by remember { mutableStateOf<IntSize?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = size
            }
    ) {
        // Dimmed background with cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height
            val padding = 16.dp.toPx()
            
            // Draw full-screen dimmed overlay
            val overlayPath = Path().apply {
                addRect(Rect(Offset.Zero, Size(screenWidth, screenHeight)))
            }
            
            // Create cutout for target if bounds are available
            if (targetBounds != null) {
                val cutoutPath = Path().apply {
                    val cutoutRect = Rect(
                        left = (targetBounds.left - padding).coerceAtLeast(0f),
                        top = (targetBounds.top - padding).coerceAtLeast(0f),
                        right = (targetBounds.right + padding).coerceAtMost(screenWidth),
                        bottom = (targetBounds.bottom + padding).coerceAtMost(screenHeight)
                    )
                    val roundRect = RoundRect(
                        rect = cutoutRect,
                        radiusX = 16.dp.toPx(),
                        radiusY = 16.dp.toPx()
                    )
                    addRoundRect(roundRect)
                }
                
                // Use PathOperation.Difference to create cutout
                val finalPath = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = overlayPath,
                    path2 = cutoutPath
                )
                
                drawPath(
                    path = finalPath,
                    color = Color.Black.copy(alpha = 0.6f)
                )
                
                // Draw highlight border around cutout
                val highlightRect = Rect(
                    left = (targetBounds.left - padding).coerceAtLeast(0f),
                    top = (targetBounds.top - padding).coerceAtLeast(0f),
                    right = (targetBounds.right + padding).coerceAtMost(screenWidth),
                    bottom = (targetBounds.bottom + padding).coerceAtMost(screenHeight)
                )
                drawRoundRect(
                    color = colorScheme.primary.copy(alpha = 0.8f),
                    topLeft = Offset(highlightRect.left, highlightRect.top),
                    size = Size(highlightRect.width, highlightRect.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            } else {
                // No target bounds, just draw full overlay
                drawPath(
                    path = overlayPath,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
        
        // Tooltip bubble
        if (targetBounds != null && screenSize != null) {
            val screenHeight = screenSize!!.height.toFloat()
            val tooltipY = if (targetBounds.top > screenHeight / 2) {
                // Target is in bottom half, show tooltip above
                targetBounds.top - with(density) { 120.dp.toPx() }
            } else {
                // Target is in top half, show tooltip below
                targetBounds.bottom + with(density) { 20.dp.toPx() }
            }
            
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp)
                    .offset(y = with(density) { tooltipY.toDp() }),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Decorative icon/emoji
                    Text(
                        text = if (isLastStep) "✨" else "👉",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = tooltipText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = if (isLastStep) onDone else onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            if (isLastStep) "Got it!" else "Next",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

