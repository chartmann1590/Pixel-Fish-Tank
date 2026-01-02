package com.charles.virtualpet.fishtank.domain.model

data class Settings(
    val notificationsEnabled: Boolean = false,
    val reminderTimes: List<String> = emptyList() // Format: "HH:mm" (e.g., "09:00", "18:00")
)

