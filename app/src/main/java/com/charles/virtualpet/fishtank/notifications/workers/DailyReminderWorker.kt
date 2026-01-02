package com.charles.virtualpet.fishtank.notifications.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.notifications.NotificationBuilder
import com.charles.virtualpet.fishtank.notifications.NotificationChannels
import com.charles.virtualpet.fishtank.notifications.NotificationIds
import com.charles.virtualpet.fishtank.notifications.NotificationPrefs
import com.charles.virtualpet.fishtank.notifications.NotificationScheduler
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = GameStateRepository(applicationContext)
        val notificationPrefs = NotificationPrefs(applicationContext)
        val gameState = repository.gameState.first()
        val settings = gameState.settings

        // Check if notifications are enabled
        if (!settings.notificationsEnabled || !settings.dailyReminderEnabled) {
            return Result.success()
        }

        // Check if notification permission is granted
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return Result.success()
        }

        // Check if we already sent a reminder today
        val lastReminderEpoch = notificationPrefs.lastDailyReminderEpoch.first()
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (lastReminderEpoch >= todayStart) {
            // Already sent today, reschedule for tomorrow
            rescheduleForTomorrow(settings.dailyReminderTime)
            return Result.success()
        }

        // Check quiet hours
        if (settings.quietHoursEnabled && isInQuietHours(settings.quietHoursStart, settings.quietHoursEnd)) {
            // In quiet hours, reschedule for after quiet hours end
            rescheduleAfterQuietHours(settings.quietHoursEnd, settings.dailyReminderTime)
            return Result.success()
        }

        // Send notification
        NotificationChannels.createChannels(applicationContext)
        val builder = NotificationBuilder(applicationContext)
        val notification = builder.buildDailyReminderNotification()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.DAILY_REMINDER, notification)

        // Update last sent time
        notificationPrefs.updateLastDailyReminderEpoch(now)

        // Reschedule for tomorrow
        rescheduleForTomorrow(settings.dailyReminderTime)

        return Result.success()
    }

    private fun rescheduleForTomorrow(timeString: String) {
        val scheduler = NotificationScheduler(applicationContext)
        scheduler.scheduleAllWork(
            notificationsEnabled = true,
            dailyReminderEnabled = true,
            dailyReminderTime = timeString,
            statusNudgesEnabled = true // This will be handled separately
        )
    }

    private fun rescheduleAfterQuietHours(quietHoursEnd: String, reminderTime: String) {
        // Schedule for the end of quiet hours today, or tomorrow if quiet hours end is tomorrow
        val (endHour, endMinute) = parseTime(quietHoursEnd)
        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If quiet hours end has passed today, schedule for tomorrow
            if (before(now) || equals(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delayMillis = targetTime.timeInMillis - now.timeInMillis
        val delayMinutes = (delayMillis / (1000 * 60)).coerceAtLeast(1)

        // Use WorkManager to schedule a one-time work for after quiet hours
        // For simplicity, we'll just reschedule normally and let the next run check quiet hours again
        rescheduleForTomorrow(reminderTime)
    }

    private fun isInQuietHours(start: String, end: String): Boolean {
        val (startHour, startMinute) = parseTime(start)
        val (endHour, endMinute) = parseTime(end)
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute

        return if (startTimeMinutes <= endTimeMinutes) {
            // Quiet hours don't cross midnight (e.g., 22:00 to 08:00)
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes < endTimeMinutes
        } else {
            // Quiet hours cross midnight (e.g., 22:00 to 08:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes < endTimeMinutes
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(19, 0) // Default to 7:00 PM
        }
    }
}

