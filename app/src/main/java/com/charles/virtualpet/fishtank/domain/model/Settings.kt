package com.charles.virtualpet.fishtank.domain.model

data class Settings(
    val notificationsEnabled: Boolean = false,
    val reminderTimes: List<String> = emptyList(), // Format: "HH:mm" (e.g., "09:00", "18:00") - legacy, kept for compatibility
    val dailyReminderEnabled: Boolean = true, // Default true when notifications enabled
    val dailyReminderTime: String = "19:00", // Format: "HH:mm"
    val statusNudgesEnabled: Boolean = true, // Default true
    val persistentNotificationEnabled: Boolean = false, // Default false
    val quietHoursEnabled: Boolean = false, // Default false
    val quietHoursStart: String = "22:00", // Format: "HH:mm"
    val quietHoursEnd: String = "08:00", // Format: "HH:mm"
    val sfxEnabled: Boolean = true,
    val bgMusicEnabled: Boolean = true
)

