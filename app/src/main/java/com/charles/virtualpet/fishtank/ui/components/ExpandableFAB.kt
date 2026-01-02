package com.charles.virtualpet.fishtank.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class FABAction(
    val label: String,
    val iconRes: Int? = null,
    val onClick: () -> Unit
)

@Composable
fun ExpandableFAB(
    actions: List<FABAction>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Backdrop overlay when expanded
        val backdropAlpha by animateFloatAsState(
            targetValue = if (expanded) 0.3f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "backdrop_alpha"
        )
        
        if (expanded || backdropAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(backdropAlpha)
                    .background(Color.Black)
                    .clickable { expanded = false }
            )
        }
        
        // Sub-actions (appear when expanded)
        actions.forEachIndexed { index, action ->
            val offsetY by animateFloatAsState(
                targetValue = if (expanded) -(70f * (index + 1)) else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "fab_offset_$index"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (expanded) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "fab_alpha_$index"
            )
            
            if (expanded || alpha > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = offsetY.dp)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .alpha(alpha)
                ) {
                    // Tooltip label to the left of the button
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = {
                            action.onClick()
                            expanded = false
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        if (action.iconRes != null) {
                            Image(
                                painter = painterResource(id = action.iconRes),
                                contentDescription = action.label,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = action.label.take(1),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
        
        // Main FAB button
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = if (expanded) "✕" else "☰",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

