package com.charles.virtualpet.fishtank.ui.store

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.data.FirebaseStoreRepository
import com.charles.virtualpet.fishtank.data.ImageCacheManager
import com.charles.virtualpet.fishtank.data.SyncState
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.domain.model.Decoration
import com.charles.virtualpet.fishtank.ui.components.CachedImage
import com.charles.virtualpet.fishtank.ui.theme.PastelBlue
import com.charles.virtualpet.fishtank.ui.theme.PastelGreen
import com.charles.virtualpet.fishtank.ui.theme.PastelPink
import com.charles.virtualpet.fishtank.ui.theme.PastelPurple
import com.charles.virtualpet.fishtank.ui.theme.PastelYellow
import com.charles.virtualpet.fishtank.ui.components.AdMobBanner
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun DecorationStoreScreen(
    viewModel: GameViewModel,
    repository: FirebaseStoreRepository?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val coins = gameState.economy.coins
    val ownedDecorations = gameState.economy.inventoryItems
        .filter { it.type == com.charles.virtualpet.fishtank.domain.model.ItemType.DECORATION }
        .associateBy { it.id }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get store items (unified list)
    val allStoreItems by if (repository != null) {
        repository.getAllStoreItems().collectAsStateWithLifecycle(
            initialValue = com.charles.virtualpet.fishtank.data.DecorationStore.availableDecorations
        )
    } else {
        remember { mutableStateOf(com.charles.virtualpet.fishtank.data.DecorationStore.availableDecorations) }
    }
    
    // Sync state
    val syncState by repository?.syncState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf<SyncState>(SyncState.Idle) }
    val hasMoreItems by repository?.hasMoreItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val isLoadingMore by repository?.isLoadingMore?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    
    // Last sync timestamp
    var lastSyncTimestamp by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(repository) {
        repository?.getLastSyncTimestamp()?.let { timestamp ->
            lastSyncTimestamp = timestamp
        }
    }
    
    // Update timestamp when sync succeeds
    LaunchedEffect(syncState) {
        if (syncState is SyncState.Success) {
            lastSyncTimestamp = (syncState as SyncState.Success).timestamp
        } else if (syncState is SyncState.Error) {
            val error = syncState as SyncState.Error
            snackbarHostState.showSnackbar(
                message = error.message,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }
    
    // Animated background gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(
            PastelBlue.copy(alpha = 0.3f),
            PastelGreen.copy(alpha = 0.2f),
            PastelPink.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.background
        )
    )
    
    val listState = rememberLazyListState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .padding(bottom = 58.dp) // Space for banner (50dp) + padding
        ) {
            // Enhanced Header with gradient card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎨 Decoration Store",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customize your tank!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        // Refresh button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository?.refresh()
                                }
                            },
                            enabled = syncState !is SyncState.Loading
                        ) {
                            if (syncState is SyncState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_rotate),
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Coin display with gradient background
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PastelYellow, PastelPink)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "💰",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "$coins",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Sync status and timestamp
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (syncState) {
                            is SyncState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Syncing...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            is SyncState.Success -> {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PastelGreen
                                )
                                Text(
                                    text = formatLastSyncTime(lastSyncTimestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            is SyncState.Error -> {
                                Text(
                                    text = "⚠",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Using cached data",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            else -> {
                                lastSyncTimestamp?.let {
                                    Text(
                                        text = formatLastSyncTime(it),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                } ?: run {
                                    Text(
                                        text = "Never synced",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Decoration list with enhanced spacing
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allStoreItems) { decoration ->
                    val ownedItem = ownedDecorations[decoration.id]
                    val quantity = ownedItem?.quantity ?: 0
                    DecorationItem(
                        decoration = decoration,
                        quantity = quantity,
                        canAfford = coins >= decoration.price,
                        onPurchase = {
                            if (coins >= decoration.price) {
                                viewModel.purchaseDecoration(decoration)
                            }
                        }
                    )
                }
                
                // Load more button
                if (hasMoreItems) {
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository?.loadMoreItems()
                                }
                            },
                            enabled = !isLoadingMore,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isLoadingMore) "Loading..." else "Load More",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced back button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "← Back to Tank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // AdMob Banner
        AdMobBanner(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        )
    }
}

@Composable
private fun DecorationItem(
    decoration: Decoration,
    quantity: Int,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100), label = "scale"
    )

    // Get decoration-specific colors
    val cardGradient = when (decoration.type) {
        com.charles.virtualpet.fishtank.domain.model.DecorationType.PLANT -> 
            Brush.horizontalGradient(
                colors = listOf(
                    PastelGreen.copy(alpha = 0.3f),
                    PastelBlue.copy(alpha = 0.2f)
                )
            )
        com.charles.virtualpet.fishtank.domain.model.DecorationType.ROCK ->
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFD4D4D4).copy(alpha = 0.3f),
                    PastelPurple.copy(alpha = 0.2f)
                )
            )
        com.charles.virtualpet.fishtank.domain.model.DecorationType.TOY ->
            Brush.horizontalGradient(
                colors = listOf(
                    PastelPink.copy(alpha = 0.3f),
                    PastelYellow.copy(alpha = 0.2f)
                )
            )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = canAfford) {
                if (canAfford) {
                    isPressed = true
                    onPurchase()
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (canAfford) 6.dp else 2.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decoration image with CachedImage
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CachedImage(
                        imageUrl = decoration.imageUrl,
                        drawableRes = decoration.drawableRes,
                        contentDescription = decoration.name,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Decoration info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = decoration.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${decoration.price} coins",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (canAfford) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    
                    if (quantity > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = PastelGreen.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✓ Owned: $quantity",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Enhanced purchase button
                Button(
                    onClick = onPurchase,
                    enabled = canAfford,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (canAfford) 4.dp else 0.dp
                    )
                ) {
                    Text(
                        text = if (canAfford) "Buy" else "💰",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatLastSyncTime(timestamp: Long?): String {
    if (timestamp == null) return "Never synced"
    
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "Last updated: $minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "Last updated: $hours hour${if (hours > 1) "s" else ""} ago"
        else -> "Last updated: $days day${if (days > 1) "s" else ""} ago"
    }
}
