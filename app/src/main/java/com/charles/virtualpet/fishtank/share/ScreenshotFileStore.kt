package com.charles.virtualpet.fishtank.share

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manages storage of screenshot files in the app's cache directory.
 * Automatically cleans up old screenshots, keeping only the last 5.
 */
object ScreenshotFileStore {
    private const val SCREENSHOT_DIR = "screenshots"
    private const val MAX_SCREENSHOTS = 5
    
    /**
     * Saves a bitmap as a PNG file in the cache directory.
     * 
     * @param context Application context
     * @param bitmap Bitmap to save
     * @return File reference to the saved screenshot, or null if save fails
     */
    fun saveScreenshot(context: Context, bitmap: Bitmap): File? {
        return try {
            // Ensure screenshots directory exists
            val screenshotsDir = File(context.cacheDir, SCREENSHOT_DIR)
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs()
            }
            
            // Generate unique filename with timestamp
            val timestamp = System.currentTimeMillis()
            val filename = "tank_screenshot_$timestamp.png"
            val file = File(screenshotsDir, filename)
            
            // Save bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            
            // Clean up old screenshots
            cleanupOldScreenshots(context)
            
            Log.d("ScreenshotFileStore", "Screenshot saved: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            Log.e("ScreenshotFileStore", "Failed to save screenshot", e)
            null
        } catch (e: Exception) {
            Log.e("ScreenshotFileStore", "Unexpected error saving screenshot", e)
            null
        }
    }
    
    /**
     * Cleans up old screenshots, keeping only the last MAX_SCREENSHOTS files.
     * Files are sorted by modification time, with oldest deleted first.
     */
    fun cleanupOldScreenshots(context: Context) {
        try {
            val screenshotsDir = File(context.cacheDir, SCREENSHOT_DIR)
            if (!screenshotsDir.exists()) {
                return
            }
            
            val screenshots = screenshotsDir.listFiles { _, name ->
                name.startsWith("tank_screenshot_") && name.endsWith(".png")
            }
            
            if (screenshots != null && screenshots.size > MAX_SCREENSHOTS) {
                // Sort by modification time (oldest first)
                screenshots.sortBy { it.lastModified() }
                
                // Delete oldest files
                val filesToDelete = screenshots.size - MAX_SCREENSHOTS
                for (i in 0 until filesToDelete) {
                    screenshots[i].delete()
                    Log.d("ScreenshotFileStore", "Deleted old screenshot: ${screenshots[i].name}")
                }
            }
        } catch (e: Exception) {
            Log.e("ScreenshotFileStore", "Failed to cleanup old screenshots", e)
        }
    }
    
    /**
     * Gets the screenshots directory.
     */
    fun getScreenshotsDirectory(context: Context): File {
        return File(context.cacheDir, SCREENSHOT_DIR)
    }
}

