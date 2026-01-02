package com.charles.virtualpet.fishtank.backup

import kotlinx.serialization.Serializable

@Serializable
data class GameStateExport(
    val fishState: FishStateExport,
    val economy: EconomyExport,
    val tankLayout: TankLayoutExport,
    val dailyTasks: DailyTasksStateExport,
    val settings: SettingsExport,
    val minigameScores: MinigameScoresExport
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
    val reminderTimes: List<String>
)

@Serializable
data class MinigameScoresExport(
    val bubblePop: Int,
    val timingBar: Int,
    val cleanupRush: Int
)

