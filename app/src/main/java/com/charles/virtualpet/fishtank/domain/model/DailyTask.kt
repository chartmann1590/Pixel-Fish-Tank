package com.charles.virtualpet.fishtank.domain.model

data class DailyTask(
    val id: String,
    val name: String,
    val description: String,
    val rewardCoins: Int,
    val rewardXP: Int,
    var isCompleted: Boolean = false
)

enum class TaskType {
    FEED_FISH,
    CLEAN_TANK,
    PLAY_MINIGAME,
    PLACE_DECORATION
}

data class DailyTasksState(
    val tasks: List<DailyTask> = emptyList(),
    val lastResetDate: String = "", // Format: YYYY-MM-DD
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String = "" // Format: YYYY-MM-DD
)

