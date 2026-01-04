package com.charles.virtualpet.fishtank.ui.rewards

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.ui.components.RewardedInterstitialAdManager
import com.charles.virtualpet.fishtank.ui.theme.PastelBlue
import com.charles.virtualpet.fishtank.ui.theme.PastelGreen
import com.charles.virtualpet.fishtank.ui.theme.PastelPink
import com.charles.virtualpet.fishtank.ui.theme.PastelPurple
import com.charles.virtualpet.fishtank.ui.theme.PastelYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun RewardsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val coins = gameState.economy.coins
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var adsWatchedCount by remember { mutableStateOf(0) }
    var remainingAds by remember { mutableStateOf(6) }
    var timeUntilReset by remember { mutableStateOf(0L) }
    var canWatchAd by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }
    var coinsEarnedThisSession by remember { mutableStateOf(0) }
    
    // Ad manager
    val adManager = remember {
        RewardedInterstitialAdManager(
            context = context,
            onRewardEarned = {
                scope.launch {
                    viewModel.recordRewardedAdWatch()
                    adsWatchedCount = 6 - viewModel.getRemainingRewardedAdsCount()
                    remainingAds = viewModel.getRemainingRewardedAdsCount()
                    canWatchAd = viewModel.canWatchRewardedAd()
                    coinsEarnedThisSession += 10
                    snackbarHostState.showSnackbar("You earned 10 coins! 🎉")
                }
            },
            onAdFailedToLoad = { error ->
                scope.launch {
                    snackbarHostState.showSnackbar("Ad failed to load: $error")
                    isAdLoading = false
                }
            },
            onAdDismissed = {
                isAdLoading = false
            }
        )
    }
    
    // Load ad on screen start
    LaunchedEffect(Unit) {
        adManager.loadAd()
    }
    
    // Update ad status
    LaunchedEffect(Unit) {
        while (true) {
            remainingAds = viewModel.getRemainingRewardedAdsCount()
            adsWatchedCount = 6 - remainingAds
            timeUntilReset = viewModel.getTimeUntilRewardedAdReset()
            canWatchAd = viewModel.canWatchRewardedAd() && adManager.isAdLoaded()
            delay(1000) // Update every second for countdown
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header Card
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
                    Text(
                        text = "💰 Free Coins",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Watch ads to earn coins!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Current coins display
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
                                text = "Current Coins: $coins",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Each ad gives you 10 coins",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• You can watch 6 ads every 6 hours",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Ads watched: $adsWatchedCount / 6",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (adsWatchedCount < 6) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    
                    if (timeUntilReset > 0 && adsWatchedCount >= 6) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val hours = TimeUnit.MILLISECONDS.toHours(timeUntilReset)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntilReset) % 60
                        Text(
                            text = "⏰ Next reset in: ${hours}h ${minutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else if (adsWatchedCount < 6) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "✨ $remainingAds ads remaining!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PastelGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (coinsEarnedThisSession > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = PastelGreen.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "🎉 Earned this session: $coinsEarnedThisSession coins",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Watch Ad Button
            Button(
                onClick = {
                    if (canWatchAd && !isAdLoading) {
                        isAdLoading = true
                        adManager.showAd()
                    } else if (!adManager.isAdLoaded()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Ad is loading, please wait...")
                            adManager.loadAd()
                        }
                    }
                },
                enabled = canWatchAd && !isAdLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canWatchAd && !isAdLoading) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    }
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (canWatchAd && !isAdLoading) 4.dp else 0.dp
                )
            ) {
                if (isAdLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Loading ad...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!canWatchAd) {
                    Text(
                        text = if (adsWatchedCount >= 6) "All ads watched! Wait for reset" else "Ad not ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "▶ Watch Ad (+10 coins)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Back Button
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
                    text = "← Back",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

