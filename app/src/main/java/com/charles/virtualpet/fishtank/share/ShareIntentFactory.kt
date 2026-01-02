package com.charles.virtualpet.fishtank.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Factory for creating share intents with image attachments and pre-filled text.
 */
object ShareIntentFactory {
    
    /**
     * Creates a share intent with an image file and optional text.
     * 
     * @param context Application context
     * @param imageFile File containing the image to share
     * @param shareText Optional pre-filled text to include in the share
     * @return Intent ready to launch with startActivity(), or null if URI generation fails
     */
    fun createShareIntent(
        context: Context,
        imageFile: File,
        shareText: String? = null
    ): Intent? {
        return try {
            // Get FileProvider URI
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(
                context,
                authority,
                imageFile
            )
            
            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                if (shareText != null) {
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Wrap in chooser
            Intent.createChooser(shareIntent, null)
        } catch (e: Exception) {
            android.util.Log.e("ShareIntentFactory", "Failed to create share intent", e)
            null
        }
    }
}

