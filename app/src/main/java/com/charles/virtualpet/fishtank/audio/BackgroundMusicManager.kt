package com.charles.virtualpet.fishtank.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.charles.virtualpet.fishtank.R

class BackgroundMusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isEnabled: Boolean = true
    private var isPlaying: Boolean = false
    
    private val MUSIC_VOLUME = 0.08f // Very soft volume (8% of max)
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            stop()
        }
    }
    
    fun start() {
        if (!isEnabled || isPlaying) {
            return
        }
        
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.bg_music)
                mediaPlayer?.let { player ->
                    player.isLooping = true
                    player.setVolume(MUSIC_VOLUME, MUSIC_VOLUME)
                    player.setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        mp.release()
                        mediaPlayer = null
                        false
                    }
                }
            }
            
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                    Log.d(TAG, "Background music started")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start background music", e)
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
    }
    
    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
            }
            isPlaying = false
            Log.d(TAG, "Background music stopped")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop background music", e)
        }
    }
    
    fun pause() {
        try {
            mediaPlayer?.pause()
            isPlaying = false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to pause background music", e)
        }
    }
    
    fun resume() {
        if (!isEnabled) {
            return
        }
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resume background music", e)
        }
    }
    
    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            Log.d(TAG, "Background music released")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release background music", e)
        }
    }
    
    companion object {
        private const val TAG = "BackgroundMusicManager"
    }
}

