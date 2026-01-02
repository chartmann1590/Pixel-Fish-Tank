package com.charles.virtualpet.fishtank.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View

/**
 * Captures a screenshot of the tank playable area composable.
 * Uses View.draw() method to capture the composable as a bitmap.
 * Composites the tank background behind the captured content for screenshots.
 */
object TankScreenshotCapturer {
    
    /**
     * Captures the tank playable area from a Compose view and composites it with the tank background.
     * 
     * @param context Application context
     * @param view The root view containing the composable to capture
     * @param width Width of the area to capture in pixels
     * @param height Height of the area to capture in pixels
     * @param backgroundResId Resource ID of the tank background drawable
     * @return Bitmap of the captured area with background, or null if capture fails
     */
    fun captureView(
        context: Context,
        view: View,
        width: Int,
        height: Int,
        backgroundResId: Int
    ): Bitmap? {
        if (width <= 0 || height <= 0) {
            return null
        }
        
        return try {
            // Create bitmap for final result
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw tank background first
            val backgroundBitmap = BitmapFactory.decodeResource(context.resources, backgroundResId)
            if (backgroundBitmap != null) {
                // Scale background to fill the canvas while maintaining aspect ratio (crop to fit)
                val matrix = Matrix()
                val scaleX = width.toFloat() / backgroundBitmap.width
                val scaleY = height.toFloat() / backgroundBitmap.height
                val scale = scaleX.coerceAtLeast(scaleY) // Use larger scale to fill (crop)
                
                matrix.setScale(scale, scale)
                
                // Center the scaled background
                val scaledWidth = backgroundBitmap.width * scale
                val scaledHeight = backgroundBitmap.height * scale
                val dx = (width - scaledWidth) / 2f
                val dy = (height - scaledHeight) / 2f
                matrix.postTranslate(dx, dy)
                
                canvas.drawBitmap(backgroundBitmap, matrix, null)
            }
            
            // Draw the playable area content (fish + decorations) on top
            view.draw(canvas)
            
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("TankScreenshotCapturer", "Failed to capture view", e)
            null
        }
    }
}

