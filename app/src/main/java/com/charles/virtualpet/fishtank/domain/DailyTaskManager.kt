package com.charles.virtualpet.fishtank.domain

import com.charles.virtualpet.fishtank.domain.model.DailyTask
import com.charles.virtualpet.fishtank.domain.model.DailyTasksState
import com.charles.virtualpet.fishtank.domain.model.TaskType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DailyTaskManager {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun generateDailyTasks(): List<DailyTask> {
        return listOf(
            DailyTask(
                id = "feed_fish",
                name = "Feed Your Fish",
                description = "Feed your fish once",
                rewardCoins = 10,
                rewardXP = 5,
                isCompleted = false
            ),
            DailyTask(
                id = "clean_tank",
                name = "Clean the Tank",
                description = "Clean your fish tank",
                rewardCoins = 15,
                rewardXP = 10,
                isCompleted = false
            ),
            DailyTask(
                id = "play_minigame",
                name = "Play Mini-Game",
                description = "Complete a mini-game",
                rewardCoins = 20,
                rewardXP = 15,
                isCompleted = false
            ),
            DailyTask(
                id = "decorate_tank",
                name = "Decorate Tank",
                description = "Place a decoration in your tank",
                rewardCoins = 25,
                rewardXP = 20,
                isCompleted = false
            )
        )
    }

    fun shouldResetTasks(currentState: DailyTasksState): Boolean {
        val today = getCurrentDate()
        return currentState.lastResetDate != today
    }

    fun resetTasksIfNeeded(currentState: DailyTasksState): DailyTasksState {
        if (shouldResetTasks(currentState)) {
            val today = getCurrentDate()
            val yesterday = getYesterdayDate()
            
            // Check if streak should continue
            val newStreak = if (currentState.lastCompletedDate == yesterday) {
                // Consecutive day - increment streak
                val newStreakValue = currentState.currentStreak + 1
                newStreakValue
            } else if (currentState.lastCompletedDate == today) {
                // Already completed today, keep streak
                currentState.currentStreak
            } else {
                // Streak broken
                0
            }
            
            val newLongestStreak = maxOf(currentState.longestStreak, newStreak)
            
            return DailyTasksState(
                tasks = generateDailyTasks(),
                lastResetDate = today,
                currentStreak = newStreak,
                longestStreak = newLongestStreak,
                lastCompletedDate = if (currentState.lastCompletedDate == today) today else ""
            )
        }
        return currentState
    }

    fun completeTask(
        taskId: String,
        currentState: DailyTasksState
    ): DailyTasksState {
        val today = getCurrentDate()
        val updatedTasks = currentState.tasks.map { task ->
            if (task.id == taskId && !task.isCompleted) {
                task.copy(isCompleted = true)
            } else {
                task
            }
        }
        
        return currentState.copy(
            tasks = updatedTasks,
            lastCompletedDate = today
        )
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
}

