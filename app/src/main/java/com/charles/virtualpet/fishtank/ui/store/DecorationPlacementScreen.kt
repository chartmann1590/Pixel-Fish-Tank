package com.charles.virtualpet.fishtank.ui.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.charles.virtualpet.fishtank.ui.tank.TankDimensions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.data.DecorationStore
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.domain.model.InventoryItem
import com.charles.virtualpet.fishtank.domain.model.ItemType

@Composable
fun DecorationPlacementScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val ownedDecorations = gameState.economy.inventoryItems
        .filter { it.type == ItemType.DECORATION }
    val placedDecorations = gameState.tankLayout.placedDecorations
    
    var selectedDecorationId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Place Decorations",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (ownedDecorations.isEmpty()) {
            Text(
                text = "You don't have any decorations yet!\nVisit the store to buy some.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            // Available decorations to place
            Text(
                text = "Tap a decoration to place it, then tap on the tank:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(ownedDecorations) { item ->
                    val decoration = DecorationStore.getDecorationById(item.id)
                    if (decoration != null) {
                        val imageResId = when (decoration.drawableRes) {
                            "decoration_plant" -> R.drawable.decoration_plant
                            "decoration_rock" -> R.drawable.decoration_rock
                            "decoration_toy" -> R.drawable.decoration_toy
                            else -> R.drawable.decoration_plant
                        }
                        
                        val hasQuantity = item.quantity > 0
                        val isSelected = selectedDecorationId == decoration.id
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(80.dp)
                                    .then(
                                        if (hasQuantity) {
                                            Modifier.clickable {
                                                selectedDecorationId = decoration.id
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = CircleShape,
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = if (hasQuantity) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = decoration.name,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .then(
                                                if (!hasQuantity) {
                                                    Modifier.alpha(0.4f)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                    if (isSelected && hasQuantity) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                    // Inventory count badge in top-right corner
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                if (hasQuantity) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.error
                                                },
                                                shape = CircleShape
                                            )
                                            .size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.quantity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            // Show quantity below the decoration
                            Text(
                                text = "x${item.quantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasQuantity) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Tank area for placement - MUST MATCH MAIN SCREEN EXACTLY (fillMaxWidth, same height)
            var containerSize by remember { mutableStateOf<IntSize?>(null) }
            val density = LocalDensity.current
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TankDimensions.TANK_HEIGHT)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { size ->
                        containerSize = size
                    }
            ) {
                // Tank background
                Image(
                    painter = painterResource(id = R.drawable.tank_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Tap detection overlay - covers entire container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                if (selectedDecorationId != null) {
                                    // tapOffset is relative to THIS Box, size is the size of THIS Box
                                    // Use size from pointerInput block - this matches the actual tap area
                                    val x = (tapOffset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    val y = (tapOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                    viewModel.placeDecoration(selectedDecorationId!!, x, y)
                                    // Complete decorate task if this is first placement
                                    if (placedDecorations.isEmpty()) {
                                        viewModel.completeDecorateTask()
                                    }
                                    selectedDecorationId = null
                                }
                            }
                        }
                )
                
                // Placed decorations - use FIXED dimensions matching main screen
                placedDecorations.forEach { placed ->
                    val decoration = DecorationStore.getDecorationById(placed.decorationId)
                    if (decoration != null) {
                        val imageResId = when (decoration.drawableRes) {
                            "decoration_plant" -> R.drawable.decoration_plant
                            "decoration_rock" -> R.drawable.decoration_rock
                            "decoration_toy" -> R.drawable.decoration_toy
                            else -> R.drawable.decoration_plant
                        }
                        
                        val decorationSize = 40.dp
                        
                        // Use ACTUAL measured container size from parent Box - must match what tap used
                        val containerWidthPx = containerSize?.width?.toFloat() ?: with(density) { TankDimensions.TANK_WIDTH.toPx() }
                        val containerHeightPx = containerSize?.height?.toFloat() ?: with(density) { TankDimensions.TANK_HEIGHT.toPx() }
                        val xPos = placed.x * containerWidthPx
                        val yPos = placed.y * containerHeightPx
                        
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
                                    viewModel.removeDecoration(placed.id)
                                }
                        )
                    }
                }
            }

            Text(
                text = "Tap placed decorations to remove them",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Tank")
        }
    }
}

