package com.charles.virtualpet.fishtank.ui.levelup

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.share.ScreenshotFileStore
import com.charles.virtualpet.fishtank.share.ShareIntentFactory
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Level-up congratulations screen that appears when the fish levels up.
 * Shows the happy fish, new level, XP progress, and share functionality.
 */
@Composable
fun LevelUpScreen(
    newLevel: Int,
    currentXP: Int,
    xpForNextLevel: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    var cardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    // Calculate XP progress
    val xpForCurrentLevel = if (newLevel > 1) {
        // XP required for current level
        100 * (newLevel - 1) * newLevel / 2
    } else {
        0
    }
    val xpProgress = currentXP - xpForCurrentLevel
    val xpNeeded = xpForNextLevel - xpForCurrentLevel
    val xpRemaining = xpNeeded - xpProgress
    
    // Animation for celebration
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .alpha(alpha)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .onGloballyPositioned { coordinates ->
                        cardCoordinates = coordinates
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "🎉 Congratulations! 🎉",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Subtitle
                    Text(
                        text = "Your fish leveled up!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Happy Fish Image with animation
                    Image(
                        painter = painterResource(id = R.drawable.fish_happy),
                        contentDescription = "Happy Fish",
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scale),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Level Display
                    Text(
                        text = "Level $newLevel",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // XP Progress
                    Text(
                        text = "$xpProgress / $xpNeeded XP",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // XP Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((xpProgress.toFloat() / xpNeeded).coerceIn(0f, 1f))
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "$xpRemaining XP until Level ${newLevel + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Buttons Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Share Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    shareLevelUpScreenshot(
                                        context = context,
                                        level = newLevel,
                                        xpProgress = xpProgress,
                                        xpNeeded = xpNeeded,
                                        xpRemaining = xpRemaining,
                                        view = view,
                                        coordinates = cardCoordinates
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "📤 Share Achievement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Continue Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun shareLevelUpScreenshot(
    context: Context,
    level: Int,
    xpProgress: Int,
    xpNeeded: Int,
    xpRemaining: Int,
    view: View,
    coordinates: LayoutCoordinates?
) {
    try {
        // Try to capture the dialog view, otherwise create programmatically
        val bitmap = if (coordinates != null && coordinates.size.width > 0 && coordinates.size.height > 0) {
            // Try to capture from the view hierarchy
            captureDialogView(view, coordinates) ?: createLevelUpBitmap(
                width = coordinates.size.width,
                height = coordinates.size.height,
                level = level,
                xpProgress = xpProgress,
                xpNeeded = xpNeeded,
                xpRemaining = xpRemaining,
                context = context
            )
        } else {
            // Create programmatically with default size
            createLevelUpBitmap(
                width = 800,
                height = 1200,
                level = level,
                xpProgress = xpProgress,
                xpNeeded = xpNeeded,
                xpRemaining = xpRemaining,
                context = context
            )
        }
        
        if (bitmap == null) return
        
        // Save to file
        val screenshotFile = ScreenshotFileStore.saveScreenshot(context, bitmap)
        if (screenshotFile == null) return
        
        // Create share text
        val shareText = context.getString(
            R.string.share_level_up_text,
            level,
            xpRemaining
        ) + "\n\nhttps://pixel-fish-tank.web.app/"
        
        // Create and launch share intent
        val shareIntent = ShareIntentFactory.createShareIntent(
            context,
            screenshotFile,
            shareText
        )
        
        if (shareIntent != null) {
            context.startActivity(shareIntent)
        }
    } catch (e: Exception) {
        android.util.Log.e("LevelUpScreen", "Failed to share screenshot", e)
    }
}

private fun captureDialogView(view: View, coordinates: LayoutCoordinates): Bitmap? {
    return try {
        // Find the dialog root view
        var rootView = view.rootView
        while (rootView.parent is View) {
            rootView = rootView.parent as View
        }
        
        // Try to find the dialog content view
        val dialogView = rootView.findViewById<View>(android.R.id.content)
        if (dialogView != null) {
            dialogView.isDrawingCacheEnabled = true
            dialogView.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(dialogView.drawingCache)
            dialogView.isDrawingCacheEnabled = false
            bitmap
        } else {
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("LevelUpScreen", "Failed to capture dialog view", e)
        null
    }
}

private fun createLevelUpBitmap(
    width: Int,
    height: Int,
    level: Int,
    xpProgress: Int,
    xpNeeded: Int,
    xpRemaining: Int,
    context: Context
): Bitmap? {
    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Draw background (white)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        // Load and draw happy fish asset
        val fishBitmap = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.fish_happy
        )
        var fishY = 280f
        var fishHeight = 0f
        if (fishBitmap != null) {
            // Scale fish to appropriate size (about 200dp equivalent)
            val fishSize = (width * 0.3f).coerceAtMost(height * 0.25f)
            val scaledFish = android.graphics.Bitmap.createScaledBitmap(
                fishBitmap,
                fishSize.toInt(),
                (fishSize * (fishBitmap.height.toFloat() / fishBitmap.width)).toInt(),
                true
            )
            // Draw fish centered horizontally, positioned after title
            val fishX = (width - scaledFish.width) / 2f
            fishY = 280f
            fishHeight = scaledFish.height.toFloat()
            canvas.drawBitmap(scaledFish, fishX, fishY, null)
        }
        
        // Draw text with proper styling
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        // Title
        paint.color = android.graphics.Color.parseColor("#6BB6FF")
        paint.textSize = 48f
        paint.isFakeBoldText = true
        canvas.drawText("🎉 Congratulations! 🎉", width / 2f, 150f, paint)
        
        // Subtitle
        paint.textSize = 32f
        paint.color = android.graphics.Color.parseColor("#2C3E50")
        paint.isFakeBoldText = false
        canvas.drawText("Your fish leveled up!", width / 2f, 220f, paint)
        
        // Level (positioned after fish)
        val levelY = if (fishBitmap != null) {
            fishY + fishHeight + 60f
        } else {
            600f
        }
        paint.textSize = 72f
        paint.color = android.graphics.Color.parseColor("#6BB6FF")
        paint.isFakeBoldText = true
        canvas.drawText("Level $level", width / 2f, levelY, paint)
        
        // XP info
        paint.textSize = 28f
        paint.color = android.graphics.Color.parseColor("#2C3E50")
        paint.isFakeBoldText = false
        canvas.drawText("$xpProgress / $xpNeeded XP", width / 2f, levelY + 100f, paint)
        
        // XP remaining
        paint.textSize = 20f
        paint.color = android.graphics.Color.parseColor("#7F8C8D")
        canvas.drawText("$xpRemaining XP until Level ${level + 1}", width / 2f, levelY + 150f, paint)
        
        bitmap
    } catch (e: Exception) {
        android.util.Log.e("LevelUpScreen", "Failed to create bitmap", e)
        null
    }
}

