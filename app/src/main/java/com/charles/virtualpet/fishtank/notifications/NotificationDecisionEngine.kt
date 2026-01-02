package com.charles.virtualpet.fishtank.notifications

import com.charles.virtualpet.fishtank.domain.model.FishState
import com.charles.virtualpet.fishtank.domain.model.Settings
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Decision engine for status-based notifications with anti-spam rules.
 */
class NotificationDecisionEngine(
    private val notificationPrefs: NotificationPrefs
) {

    companion object {
        const val STATUS_TYPE_HUNGRY = "hungry"
        const val STATUS_TYPE_DIRTY = "dirty"
        const val STATUS_TYPE_SAD = "sad"
        
        const val STATUS_THRESHOLD = 25f // Notify if stat < 25
        const val COOLDOWN_HOURS = 8L // 8 hours cooldown per type
        const val MAX_STATUS_NUDGES_PER_DAY = 2
        const val APP_OPEN_COOLDOWN_HOURS = 3L // Skip if app opened within last 3 hours
    }

    /**
     * Determine which status notification (if any) should be sent.
     * Returns the status type to notify about, or null if no notification should be sent.
     */
    suspend fun shouldSendStatusNudge(
        fishState: FishState,
        settings: Settings,
        lastAppOpenEpoch: Long
    ): String? {
        // Check if status nudges are enabled
        if (!settings.statusNudgesEnabled || !settings.notificationsEnabled) {
            return null
        }

        // Check if app was opened recently (within last 3 hours)
        val now = System.currentTimeMillis()
        val appOpenCooldownMillis = APP_OPEN_COOLDOWN_HOURS * 60 * 60 * 1000
        if (now - lastAppOpenEpoch < appOpenCooldownMillis) {
            return null // User recently opened app, skip notification
        }

        // Check quiet hours
        if (settings.quietHoursEnabled && isInQuietHours(settings.quietHoursStart, settings.quietHoursEnd)) {
            return null // In quiet hours, defer notification
        }

        // Reset daily count if needed (at midnight)
        if (notificationPrefs.shouldResetDailyCount()) {
            notificationPrefs.resetStatusNudgesSentTodayCount()
        }

        // Check daily limit
        val nudgesSentToday = notificationPrefs.statusNudgesSentTodayCount.first()
        if (nudgesSentToday >= MAX_STATUS_NUDGES_PER_DAY) {
            return null // Already sent max nudges today
        }

        // Check each status type and find the one that needs attention and is past cooldown
        val lastNudgeEpochs = notificationPrefs.lastStatusNudgeEpochByType.first()
        val cooldownMillis = COOLDOWN_HOURS * 60 * 60 * 1000

        // Priority: hungry > dirty > sad
        val statusChecks = listOf(
            STATUS_TYPE_HUNGRY to (fishState.hunger < STATUS_THRESHOLD),
            STATUS_TYPE_DIRTY to (fishState.cleanliness < STATUS_THRESHOLD),
            STATUS_TYPE_SAD to (fishState.happiness < STATUS_THRESHOLD)
        )

        for ((type, needsAttention) in statusChecks) {
            if (needsAttention) {
                val lastNudgeEpoch = lastNudgeEpochs[type] ?: 0L
                val timeSinceLastNudge = now - lastNudgeEpoch
                
                if (timeSinceLastNudge >= cooldownMillis) {
                    // This status needs attention and cooldown has passed
                    return type
                }
            }
        }

        return null // No status needs attention or all are in cooldown
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
            // Quiet hours don't cross midnight
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes < endTimeMinutes
        } else {
            // Quiet hours cross midnight
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes < endTimeMinutes
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(22, 0) // Default
        }
    }
}

