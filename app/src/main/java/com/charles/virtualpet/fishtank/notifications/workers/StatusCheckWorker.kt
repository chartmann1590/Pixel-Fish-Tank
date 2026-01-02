package com.charles.virtualpet.fishtank.notifications.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.StatDecayCalculator
import com.charles.virtualpet.fishtank.notifications.NotificationBuilder
import com.charles.virtualpet.fishtank.notifications.NotificationChannels
import com.charles.virtualpet.fishtank.notifications.NotificationDecisionEngine
import com.charles.virtualpet.fishtank.notifications.NotificationIds
import com.charles.virtualpet.fishtank.notifications.NotificationPrefs
import kotlinx.coroutines.flow.first

class StatusCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = GameStateRepository(applicationContext)
        val notificationPrefs = NotificationPrefs(applicationContext)
        val gameState = repository.gameState.first()
        val settings = gameState.settings

        // Check if notifications are enabled
        if (!settings.notificationsEnabled || !settings.statusNudgesEnabled) {
            return Result.success()
        }

        // Check if notification permission is granted
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return Result.success()
        }

        // Apply decay to get current fish state
        val currentFishState = StatDecayCalculator.calculateDecay(gameState.fishState)
        val lastAppOpenEpoch = notificationPrefs.lastAppOpenEpoch.first()

        // Use decision engine to determine if we should send a notification
        val decisionEngine = NotificationDecisionEngine(notificationPrefs)
        val statusType = decisionEngine.shouldSendStatusNudge(
            fishState = currentFishState,
            settings = settings,
            lastAppOpenEpoch = lastAppOpenEpoch
        )

        if (statusType == null) {
            return Result.success() // No notification needed
        }

        // Send the appropriate notification
        NotificationChannels.createChannels(applicationContext)
        val builder = NotificationBuilder(applicationContext)
        val notification = when (statusType) {
            NotificationDecisionEngine.STATUS_TYPE_HUNGRY -> builder.buildHungryNotification()
            NotificationDecisionEngine.STATUS_TYPE_DIRTY -> builder.buildDirtyNotification()
            NotificationDecisionEngine.STATUS_TYPE_SAD -> builder.buildSadNotification()
            else -> return Result.success()
        }

        val notificationId = when (statusType) {
            NotificationDecisionEngine.STATUS_TYPE_HUNGRY -> NotificationIds.STATUS_NUDGE_HUNGRY
            NotificationDecisionEngine.STATUS_TYPE_DIRTY -> NotificationIds.STATUS_NUDGE_DIRTY
            NotificationDecisionEngine.STATUS_TYPE_SAD -> NotificationIds.STATUS_NUDGE_SAD
            else -> NotificationIds.STATUS_NUDGE_HUNGRY
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)

        // Update tracking
        val now = System.currentTimeMillis()
        notificationPrefs.updateLastStatusNudgeEpoch(statusType, now)
        notificationPrefs.incrementStatusNudgesSentTodayCount()

        return Result.success()
    }
}

