package com.charles.virtualpet.fishtank.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.charles.virtualpet.fishtank.MainActivity
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.model.FishState
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.domain.MoodCalculator

class PersistentNotificationManager(private val context: Context) {

    companion object {
        const val ACTION_FEED = "com.charles.virtualpet.fishtank.ACTION_FEED"
        const val ACTION_CLEAN = "com.charles.virtualpet.fishtank.ACTION_CLEAN"
        const val ACTION_OPEN = "com.charles.virtualpet.fishtank.ACTION_OPEN"
    }

    /**
     * Update or show the persistent notification with current fish status.
     */
    fun updateNotification(gameState: GameState) {
        val settings = gameState.settings
        if (!settings.notificationsEnabled || !settings.persistentNotificationEnabled) {
            cancelNotification()
            return
        }

        // Check permission
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val fishState = gameState.fishState
        val mood = MoodCalculator.calculateMood(fishState)
        val moodEmoji = getMoodEmoji(mood)
        val moodText = getMoodText(mood)

        // Build notification content
        val contentText = buildString {
            append("Hunger: ${fishState.hunger.toInt()}% | ")
            append("Clean: ${fishState.cleanliness.toInt()}% | ")
            append("Happy: ${fishState.happiness.toInt()}%")
        }

        val expandedText = buildString {
            append("Level ${fishState.level} | ")
            append("${gameState.economy.coins} coins\n")
            append(contentText)
        }

        // Create actions
        val feedIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_FEED
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val feedPendingIntent = PendingIntent.getActivity(
            context,
            0,
            feedIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cleanIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_CLEAN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val cleanPendingIntent = PendingIntent.getActivity(
            context,
            1,
            cleanIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            2,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        NotificationChannels.createChannels(context)
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_PERSISTENT)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("$moodEmoji $moodText")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .addAction(
                R.drawable.fish_starter, // Use fish icon as placeholder, you may want to add specific icons
                "Feed",
                feedPendingIntent
            )
            .addAction(
                R.drawable.fish_starter,
                "Clean",
                cleanPendingIntent
            )
            .addAction(
                R.drawable.fish_starter,
                "Open",
                openPendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.PERSISTENT, notification)
    }

    /**
     * Cancel the persistent notification.
     */
    fun cancelNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NotificationIds.PERSISTENT)
    }

    private fun getMoodEmoji(mood: com.charles.virtualpet.fishtank.domain.model.FishMood): String {
        return when (mood) {
            com.charles.virtualpet.fishtank.domain.model.FishMood.HAPPY -> "😊"
            com.charles.virtualpet.fishtank.domain.model.FishMood.NEUTRAL -> "😐"
            com.charles.virtualpet.fishtank.domain.model.FishMood.HUNGRY -> "😟"
            com.charles.virtualpet.fishtank.domain.model.FishMood.DIRTY -> "😷"
            com.charles.virtualpet.fishtank.domain.model.FishMood.SAD -> "😢"
        }
    }

    private fun getMoodText(mood: com.charles.virtualpet.fishtank.domain.model.FishMood): String {
        return when (mood) {
            com.charles.virtualpet.fishtank.domain.model.FishMood.HAPPY -> "Happy"
            com.charles.virtualpet.fishtank.domain.model.FishMood.NEUTRAL -> "Neutral"
            com.charles.virtualpet.fishtank.domain.model.FishMood.HUNGRY -> "Hungry"
            com.charles.virtualpet.fishtank.domain.model.FishMood.DIRTY -> "Dirty"
            com.charles.virtualpet.fishtank.domain.model.FishMood.SAD -> "Sad"
        }
    }
}

