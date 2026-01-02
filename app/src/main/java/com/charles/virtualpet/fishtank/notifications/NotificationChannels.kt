package com.charles.virtualpet.fishtank.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_STATUS = "status"
    const val CHANNEL_PERSISTENT = "persistent"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Reminders channel (low importance)
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily check-in reminders"
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(remindersChannel)

            // Status channel (default importance)
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Fish needs attention"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(statusChannel)

            // Persistent channel (low importance, ongoing)
            val persistentChannel = NotificationChannel(
                CHANNEL_PERSISTENT,
                "Fish Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live fish status monitor"
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(persistentChannel)
        }
    }
}

