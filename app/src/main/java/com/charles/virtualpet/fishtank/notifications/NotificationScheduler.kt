package com.charles.virtualpet.fishtank.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.charles.virtualpet.fishtank.notifications.workers.DailyReminderWorker
import com.charles.virtualpet.fishtank.notifications.workers.StatusCheckWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        const val DAILY_REMINDER_WORK_NAME = "daily_reminder_work"
        const val STATUS_CHECK_WORK_NAME = "status_check_work"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule all notification work based on current settings.
     * Call this when settings change or on app startup.
     */
    fun scheduleAllWork(
        notificationsEnabled: Boolean,
        dailyReminderEnabled: Boolean,
        dailyReminderTime: String,
        statusNudgesEnabled: Boolean
    ) {
        if (!notificationsEnabled) {
            cancelAllWork()
            return
        }

        if (dailyReminderEnabled) {
            scheduleDailyReminder(dailyReminderTime)
        } else {
            cancelDailyReminder()
        }

        if (statusNudgesEnabled) {
            scheduleStatusCheck()
        } else {
            cancelStatusCheck()
        }
    }

    /**
     * Schedule daily reminder at the specified time.
     * Reschedules itself after sending.
     */
    private fun scheduleDailyReminder(timeString: String) {
        val (hour, minute) = parseTime(timeString)
        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (before(now) || equals(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delayMillis = targetTime.timeInMillis - now.timeInMillis
        val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(delayMillis).coerceAtLeast(1)

        val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag(DAILY_REMINDER_WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Schedule periodic status check worker (every 3 hours).
     */
    private fun scheduleStatusCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<StatusCheckWorker>(
            3, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag(STATUS_CHECK_WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            STATUS_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancel daily reminder work.
     */
    fun cancelDailyReminder() {
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }

    /**
     * Cancel status check work.
     */
    fun cancelStatusCheck() {
        workManager.cancelUniqueWork(STATUS_CHECK_WORK_NAME)
    }

    /**
     * Cancel all notification work.
     */
    fun cancelAllWork() {
        cancelDailyReminder()
        cancelStatusCheck()
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

