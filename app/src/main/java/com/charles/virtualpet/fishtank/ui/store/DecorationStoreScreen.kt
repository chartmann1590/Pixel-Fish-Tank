package com.charles.virtualpet.fishtank.ui.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.data.DecorationStore
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.domain.model.Decoration

@Composable
fun DecorationStoreScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val coins = gameState.economy.coins
    val ownedDecorationIds = gameState.economy.inventoryItems
        .filter { it.type == com.charles.virtualpet.fishtank.domain.model.ItemType.DECORATION }
        .map { it.id }
        .toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Decoration Store",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "💰 $coins",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Decoration list
        LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(DecorationStore.availableDecorations) { decoration ->
                DecorationItem(
                    decoration = decoration,
                    isOwned = ownedDecorationIds.contains(decoration.id),
                    canAfford = coins >= decoration.price,
                    onPurchase = {
                        if (coins >= decoration.price && !ownedDecorationIds.contains(decoration.id)) {
                            viewModel.purchaseDecoration(decoration)
                        }
                    }
                )
            }
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

@Composable
private fun DecorationItem(
    decoration: Decoration,
    isOwned: Boolean,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decoration image
            val imageResId = when (decoration.drawableRes) {
                "decoration_plant" -> R.drawable.decoration_plant
                "decoration_rock" -> R.drawable.decoration_rock
                "decoration_toy" -> R.drawable.decoration_toy
                else -> R.drawable.decoration_plant
            }
            
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = decoration.name,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.padding(16.dp))

            // Decoration info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = decoration.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "💰 ${decoration.price} coins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Purchase button
            if (isOwned) {
                Text(
                    text = "Owned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = canAfford
                ) {
                    Text("Buy")
                }
            }
        }
    }
}

