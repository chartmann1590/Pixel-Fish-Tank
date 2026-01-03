package com.charles.virtualpet.fishtank.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ImageCacheManager(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "store_images")
    
    init {
        // Create cache directory if it doesn't exist
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Gets image from cache or downloads from URL (Imgur, Firebase Storage, etc.)
     * Returns the local file path if successful, null otherwise
     */
    suspend fun getImage(imageUrl: String, itemId: String): String? = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cachedFile = File(cacheDir, "$itemId.jpg")
            if (cachedFile.exists()) {
                return@withContext cachedFile.absolutePath
            }
            
            // Download from URL (works with any HTTP/HTTPS URL)
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            // Save to cache
            FileOutputStream(cachedFile).use { it.write(bytes) }
            
            cachedFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Gets cached image file if it exists
     */
    fun getCachedImage(itemId: String): File? {
        val cachedFile = File(cacheDir, "$itemId.jpg")
        return if (cachedFile.exists()) cachedFile else null
    }
    
    /**
     * Clears the image cache
     */
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Gets cache size in bytes
     */
    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}

