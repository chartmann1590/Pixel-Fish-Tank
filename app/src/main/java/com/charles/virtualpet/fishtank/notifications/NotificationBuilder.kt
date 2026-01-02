package com.charles.virtualpet.fishtank.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.charles.virtualpet.fishtank.MainActivity
import com.charles.virtualpet.fishtank.R

class NotificationBuilder(private val context: Context) {

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun buildDailyReminderNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("Your fish misses you 🐠")
            .setContentText("Time for a quick check-in!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your fish misses you 🐠 Time for a quick check-in!"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(createOpenAppIntent())
            .setAutoCancel(true)
            .build()
    }

    fun buildHungryNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_STATUS)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("🐠 Your fish is hungry!")
            .setContentText("Feed your fish to keep it happy")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your fish is getting hungry! Feed it to keep it healthy and happy."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createOpenAppIntent())
            .setAutoCancel(true)
            .build()
    }

    fun buildDirtyNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_STATUS)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("🐠 Your tank needs cleaning!")
            .setContentText("Clean the tank to keep your fish healthy")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your tank is getting dirty! Clean it to keep your fish healthy and happy."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createOpenAppIntent())
            .setAutoCancel(true)
            .build()
    }

    fun buildSadNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_STATUS)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("🐠 Your fish is sad!")
            .setContentText("Give your fish some attention")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your fish is feeling sad! Play with it or check on it to boost its happiness."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createOpenAppIntent())
            .setAutoCancel(true)
            .build()
    }

    fun buildTestNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.fish_starter)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification from Pixel Fish Tank")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(createOpenAppIntent())
            .setAutoCancel(true)
            .build()
    }
}

