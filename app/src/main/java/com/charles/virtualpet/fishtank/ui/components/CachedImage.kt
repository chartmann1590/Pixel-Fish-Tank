package com.charles.virtualpet.fishtank.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.data.ImageCacheManager
import java.io.File

@Composable
fun CachedImage(
    imageUrl: String?,
    drawableRes: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    
    // If imageUrl is provided, use it (from Imgur/Firebase Storage)
    // Otherwise, fall back to drawable resource
    if (imageUrl != null && imageUrl.isNotBlank()) {
        val imageCacheManager = remember { 
            ImageCacheManager(context) 
        }
        
        // Try to get cached image first
        val cachedFile = remember(imageUrl) {
            // Extract item ID from URL or use hash
            val itemId = try {
                imageUrl.substringAfterLast("/").substringBeforeLast(".")
                    .ifEmpty { imageUrl.hashCode().toString() }
            } catch (e: Exception) {
                imageUrl.hashCode().toString()
            }
            imageCacheManager.getCachedImage(itemId)
        }
        
        val imageRequest = remember(imageUrl, cachedFile) {
            ImageRequest.Builder(context)
                .data(cachedFile ?: imageUrl)
                .crossfade(true)
                .error(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                .placeholder(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                .build()
        }
        
        Image(
            painter = rememberAsyncImagePainter(imageRequest),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Fallback to drawable resource
        val imageResId = when (drawableRes) {
            "decoration_plant" -> R.drawable.decoration_plant
            "decoration_rock" -> R.drawable.decoration_rock
            "decoration_toy" -> R.drawable.decoration_toy
            else -> R.drawable.decoration_plant
        }
        
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

