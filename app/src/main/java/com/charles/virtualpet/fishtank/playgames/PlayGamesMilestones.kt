package com.charles.virtualpet.fishtank.playgames

import com.charles.virtualpet.fishtank.domain.model.GameState

internal enum class PlayGamesAchievement {
    FIRST_FEED,
    SPARKLING_CLEAN,
    LEVEL_5,
    LEVEL_10,
    STREAK_3,
    STREAK_7,
    DECORATOR,
    COLLECTOR
}

internal object PlayGamesMilestones {
    fun achievementsFor(state: GameState): Set<PlayGamesAchievement> = buildSet {
        val completedTasks = state.dailyTasks.tasks
            .asSequence()
            .filter { it.isCompleted }
            .map { it.id }
            .toSet()

        if ("feed_fish" in completedTasks) add(PlayGamesAchievement.FIRST_FEED)
        if ("clean_tank" in completedTasks) add(PlayGamesAchievement.SPARKLING_CLEAN)
        if (state.fishState.level >= 5) add(PlayGamesAchievement.LEVEL_5)
        if (state.fishState.level >= 10) add(PlayGamesAchievement.LEVEL_10)
        if (state.dailyTasks.longestStreak >= 3) add(PlayGamesAchievement.STREAK_3)
        if (state.dailyTasks.longestStreak >= 7) add(PlayGamesAchievement.STREAK_7)
        if (state.tankLayout.placedDecorations.size >= 5) add(PlayGamesAchievement.DECORATOR)
        if (state.economy.inventoryItems.size >= 5) {
            add(PlayGamesAchievement.COLLECTOR)
        }
    }
}
