package com.charles.virtualpet.fishtank.backup

import kotlinx.serialization.Serializable

@Serializable
data class GameStateExport(
    val fishState: FishStateExport,
    val economy: EconomyExport,
    val tankLayout: TankLayoutExport,
    val dailyTasks: DailyTasksStateExport,
    val settings: SettingsExport,
    val minigameScores: MinigameScoresExport,
    val purchaseHistory: PurchaseHistoryExport = PurchaseHistoryExport()
)

@Serializable
data class FishStateExport(
    val hunger: Float,
    val cleanliness: Float,
    val happiness: Float,
    val level: Int,
    val xp: Int,
    val lastUpdatedEpoch: Long
)

@Serializable
data class EconomyExport(
    val coins: Int,
    val inventoryItems: List<InventoryItemExport>
)

@Serializable
data class InventoryItemExport(
    val id: String,
    val name: String,
    val type: String, // ItemType enum as string
    val quantity: Int
)

@Serializable
data class TankLayoutExport(
    val placedDecorations: List<PlacedDecorationExport>
)

@Serializable
data class PlacedDecorationExport(
    val id: String,
    val decorationId: String,
    val x: Float,
    val y: Float
)

@Serializable
data class DailyTasksStateExport(
    val tasks: List<DailyTaskExport>,
    val lastResetDate: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: String
)

@Serializable
data class DailyTaskExport(
    val id: String,
    val name: String,
    val description: String,
    val rewardCoins: Int,
    val rewardXP: Int,
    val isCompleted: Boolean
)

@Serializable
data class SettingsExport(
    val notificationsEnabled: Boolean,
    val reminderTimes: List<String>,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "19:00",
    val statusNudgesEnabled: Boolean = true,
    val persistentNotificationEnabled: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00",
    val sfxEnabled: Boolean = true,
    val bgMusicEnabled: Boolean = true,
    val hasCompletedTutorial: Boolean = false,
    val decorationsLocked: Boolean = true
)

@Serializable
data class MinigameScoresExport(
    val bubblePop: Int,
    val timingBar: Int,
    val cleanupRush: Int,
    val foodDrop: Int = 0,
    val memoryShells: Int = 0,
    val fishFollow: Int = 0
)

@Serializable
data class PurchaseExport(
    val itemId: String,
    val itemName: String,
    val price: Int,
    val type: String,
    val timestamp: Long,
    val userId: String
)

@Serializable
data class PurchaseHistoryExport(
    val purchases: List<PurchaseExport> = emptyList()
)

