package com.charles.virtualpet.fishtank.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Helper class for logging analytics events to Firebase Analytics.
 * Provides a centralized way to track user interactions and app usage.
 */
object AnalyticsHelper {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    /**
     * Initialize the analytics helper with a context.
     * Should be called once during app initialization.
     */
    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = Firebase.analytics
        }
    }

    /**
     * Log a custom event with optional parameters.
     */
    private fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        firebaseAnalytics?.let { analytics ->
            if (params != null) {
                val bundle = android.os.Bundle().apply {
                    params.forEach { (key, value) ->
                        when (value) {
                            is String -> putString(key, value)
                            is Int -> putInt(key, value)
                            is Long -> putLong(key, value)
                            is Double -> putDouble(key, value)
                            is Float -> putDouble(key, value.toDouble())
                            is Boolean -> putBoolean(key, value)
                            else -> putString(key, value.toString())
                        }
                    }
                }
                analytics.logEvent(eventName, bundle)
            } else {
                analytics.logEvent(eventName, null)
            }
        }
    }

    // Game Action Events
    fun logFeedFish() {
        logEvent("feed_fish")
    }

    fun logCleanTank() {
        logEvent("clean_tank")
    }

    fun logPlaceDecoration(decorationType: String) {
        logEvent("place_decoration", mapOf("decoration_type" to decorationType))
    }

    fun logRemoveDecoration(decorationType: String) {
        logEvent("remove_decoration", mapOf("decoration_type" to decorationType))
    }

    fun logBuyItem(itemType: String, itemName: String, price: Int) {
        logEvent("buy_item", mapOf(
            "item_type" to itemType,
            "item_name" to itemName,
            "price" to price
        ))
    }

    // Mini-Game Events
    fun logMiniGameStart(gameType: String, difficulty: String) {
        logEvent("minigame_start", mapOf(
            "game_type" to gameType,
            "difficulty" to difficulty
        ))
    }

    fun logMiniGameComplete(gameType: String, difficulty: String, score: Int, coinsEarned: Int, xpEarned: Int) {
        logEvent("minigame_complete", mapOf(
            "game_type" to gameType,
            "difficulty" to difficulty,
            "score" to score,
            "coins_earned" to coinsEarned,
            "xp_earned" to xpEarned
        ))
    }

    fun logMiniGameHighScore(gameType: String, difficulty: String, highScore: Int) {
        logEvent("minigame_high_score", mapOf(
            "game_type" to gameType,
            "difficulty" to difficulty,
            "high_score" to highScore
        ))
    }

    // Progression Events
    fun logLevelUp(newLevel: Int, totalXP: Int) {
        logEvent("level_up", mapOf(
            "new_level" to newLevel,
            "total_xp" to totalXP
        ))
    }

    fun logTaskComplete(taskId: String, coinsEarned: Int, xpEarned: Int) {
        logEvent("task_complete", mapOf(
            "task_id" to taskId,
            "coins_earned" to coinsEarned,
            "xp_earned" to xpEarned
        ))
    }

    fun logDailyStreak(streakDays: Int) {
        logEvent("daily_streak", mapOf("streak_days" to streakDays))
    }

    // Screen Navigation Events
    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    // Settings Events
    fun logSettingsChange(settingName: String, value: Any) {
        logEvent("settings_change", mapOf(
            "setting_name" to settingName,
            "value" to value.toString()
        ))
    }

    // Backup/Restore Events
    fun logBackupExport(success: Boolean) {
        logEvent("backup_export", mapOf("success" to success))
    }

    fun logBackupImport(success: Boolean) {
        logEvent("backup_import", mapOf("success" to success))
    }

    // App Lifecycle Events
    fun logAppOpen() {
        logEvent("app_open")
    }

    fun logAppBackground() {
        logEvent("app_background")
    }
}

