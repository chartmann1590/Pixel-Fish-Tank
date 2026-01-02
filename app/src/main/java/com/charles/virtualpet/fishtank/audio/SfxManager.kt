package com.charles.virtualpet.fishtank.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import com.charles.virtualpet.fishtank.R

class SfxManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var isEnabled: Boolean = true
    
    // Sound IDs for loaded sounds
    private var bubblePopSoundId: Int = 0
    private var cleanSplashSoundId: Int = 0
    private var feedNibbleSoundId: Int = 0
    private var happyChimeSoundId: Int = 0
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        try {
            soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                
                SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                SoundPool(4, AudioManager.STREAM_MUSIC, 0)
            }
            
            loadSounds()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize SoundPool", e)
            soundPool = null
        }
    }
    
    private fun loadSounds() {
        soundPool?.let { pool ->
            try {
                bubblePopSoundId = pool.load(context, R.raw.bubble_pop, 1)
                if (bubblePopSoundId == 0) {
                    Log.w(TAG, "Failed to load bubble_pop sound: resource not found or invalid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load bubble_pop sound", e)
                bubblePopSoundId = 0
            }
            
            try {
                cleanSplashSoundId = pool.load(context, R.raw.clean_splash, 1)
                if (cleanSplashSoundId == 0) {
                    Log.w(TAG, "Failed to load clean_splash sound: resource not found or invalid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load clean_splash sound", e)
                cleanSplashSoundId = 0
            }
            
            try {
                feedNibbleSoundId = pool.load(context, R.raw.feed_nibble, 1)
                if (feedNibbleSoundId == 0) {
                    Log.w(TAG, "Failed to load feed_nibble sound: resource not found or invalid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load feed_nibble sound", e)
                feedNibbleSoundId = 0
            }
            
            try {
                happyChimeSoundId = pool.load(context, R.raw.happy_chime, 1)
                if (happyChimeSoundId == 0) {
                    Log.w(TAG, "Failed to load happy_chime sound: resource not found or invalid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load happy_chime sound", e)
                happyChimeSoundId = 0
            }
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    fun play(event: SfxEvent) {
        if (!isEnabled || soundPool == null) {
            return
        }
        
        val soundId = when (event) {
            SfxEvent.BUBBLE_POP -> bubblePopSoundId
            SfxEvent.CLEAN_SPLASH -> cleanSplashSoundId
            SfxEvent.FEED_NIBBLE -> feedNibbleSoundId
            SfxEvent.HAPPY_CHIME -> happyChimeSoundId
        }
        
        if (soundId > 0) {
            try {
                soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play sound for event: $event", e)
            }
        }
    }
    
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release SoundPool", e)
        }
    }
    
    companion object {
        private const val TAG = "SfxManager"
    }
}

