package com.charles.virtualpet.fishtank.backup

import android.content.Context
import android.net.Uri
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.model.DailyTask
import com.charles.virtualpet.fishtank.domain.model.DailyTasksState
import com.charles.virtualpet.fishtank.domain.model.Economy
import com.charles.virtualpet.fishtank.domain.model.FishState
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.domain.model.InventoryItem
import com.charles.virtualpet.fishtank.domain.model.ItemType
import com.charles.virtualpet.fishtank.domain.model.PlacedDecoration
import com.charles.virtualpet.fishtank.domain.model.Settings
import com.charles.virtualpet.fishtank.domain.model.TankLayout
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class BackupRepository(private val context: Context) {
    
    suspend fun exportCurrentState(
        gameState: GameState,
        repository: GameStateRepository
    ): BackupEnvelope {
        // Get high scores
        val bubblePopScore = repository.getHighScore(MiniGameType.BUBBLE_POP).first()
        val timingBarScore = repository.getHighScore(MiniGameType.TIMING_BAR).first()
        val cleanupRushScore = repository.getHighScore(MiniGameType.CLEANUP_RUSH).first()
        
        val minigameScores = MinigameScoresExport(
            bubblePop = bubblePopScore,
            timingBar = timingBarScore,
            cleanupRush = cleanupRushScore
        )
        
        val gameStateExport = GameStateExport(
            fishState = gameState.fishState.toFishStateExport(),
            economy = gameState.economy.toEconomyExport(),
            tankLayout = gameState.tankLayout.toTankLayoutExport(),
            dailyTasks = gameState.dailyTasks.toDailyTasksStateExport(),
            settings = gameState.settings.toSettingsExport(),
            minigameScores = minigameScores
        )
        
        return BackupEnvelope(
            exportedAtEpoch = System.currentTimeMillis(),
            data = gameStateExport
        )
    }
    
    suspend fun writeBackupToUri(uri: Uri, envelope: BackupEnvelope, repository: GameStateRepository): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = BackupSerializer.serialize(envelope)
                context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                } ?: return@withContext Result.failure(Exception("Failed to open output stream"))
                
                // Update last backup time after successful write
                repository.updateLastBackupTime(System.currentTimeMillis())
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun readBackupFromUri(uri: Uri): Result<BackupEnvelope> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@withContext Result.failure(Exception("Failed to open input stream"))
                
                val envelope = BackupSerializer.deserialize(jsonString)
                Result.success(envelope)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun importState(
        envelope: BackupEnvelope,
        repository: GameStateRepository
    ): Result<Unit> {
        return try {
            val gameState = envelope.data.toGameState()
            repository.saveGameState(gameState)
            
            // Save high scores
            repository.saveHighScore(MiniGameType.BUBBLE_POP, envelope.data.minigameScores.bubblePop)
            repository.saveHighScore(MiniGameType.TIMING_BAR, envelope.data.minigameScores.timingBar)
            repository.saveHighScore(MiniGameType.CLEANUP_RUSH, envelope.data.minigameScores.cleanupRush)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions to convert from domain models to export DTOs
private fun FishState.toFishStateExport(): FishStateExport {
    return FishStateExport(
        hunger = hunger,
        cleanliness = cleanliness,
        happiness = happiness,
        level = level,
        xp = xp,
        lastUpdatedEpoch = lastUpdatedEpoch
    )
}

private fun Economy.toEconomyExport(): EconomyExport {
    return EconomyExport(
        coins = coins,
        inventoryItems = inventoryItems.map { it.toInventoryItemExport() }
    )
}

private fun InventoryItem.toInventoryItemExport(): InventoryItemExport {
    return InventoryItemExport(
        id = id,
        name = name,
        type = type.name,
        quantity = quantity
    )
}

private fun TankLayout.toTankLayoutExport(): TankLayoutExport {
    return TankLayoutExport(
        placedDecorations = placedDecorations.map { it.toPlacedDecorationExport() }
    )
}

private fun PlacedDecoration.toPlacedDecorationExport(): PlacedDecorationExport {
    return PlacedDecorationExport(
        id = id,
        decorationId = decorationId,
        x = x,
        y = y
    )
}

private fun DailyTasksState.toDailyTasksStateExport(): DailyTasksStateExport {
    return DailyTasksStateExport(
        tasks = tasks.map { it.toDailyTaskExport() },
        lastResetDate = lastResetDate,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastCompletedDate = lastCompletedDate
    )
}

private fun DailyTask.toDailyTaskExport(): DailyTaskExport {
    return DailyTaskExport(
        id = id,
        name = name,
        description = description,
        rewardCoins = rewardCoins,
        rewardXP = rewardXP,
        isCompleted = isCompleted
    )
}

private fun Settings.toSettingsExport(): SettingsExport {
    return SettingsExport(
        notificationsEnabled = notificationsEnabled,
        reminderTimes = reminderTimes
    )
}

// Extension functions to convert from export DTOs back to domain models
private fun GameStateExport.toGameState(): GameState {
    return GameState(
        fishState = fishState.toFishState(),
        economy = economy.toEconomy(),
        tankLayout = tankLayout.toTankLayout(),
        dailyTasks = dailyTasks.toDailyTasksState(),
        settings = settings.toSettings()
    )
}

private fun FishStateExport.toFishState(): FishState {
    return FishState(
        hunger = hunger,
        cleanliness = cleanliness,
        happiness = happiness,
        level = level,
        xp = xp,
        lastUpdatedEpoch = lastUpdatedEpoch
    )
}

private fun EconomyExport.toEconomy(): Economy {
    return Economy(
        coins = coins,
        inventoryItems = inventoryItems.map { it.toInventoryItem() }
    )
}

private fun InventoryItemExport.toInventoryItem(): InventoryItem {
    return InventoryItem(
        id = id,
        name = name,
        type = ItemType.valueOf(type),
        quantity = quantity
    )
}

private fun TankLayoutExport.toTankLayout(): TankLayout {
    return TankLayout(
        placedDecorations = placedDecorations.map { it.toPlacedDecoration() }
    )
}

private fun PlacedDecorationExport.toPlacedDecoration(): PlacedDecoration {
    return PlacedDecoration(
        id = id,
        decorationId = decorationId,
        x = x,
        y = y
    )
}

private fun DailyTasksStateExport.toDailyTasksState(): DailyTasksState {
    return DailyTasksState(
        tasks = tasks.map { it.toDailyTask() },
        lastResetDate = lastResetDate,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastCompletedDate = lastCompletedDate
    )
}

private fun DailyTaskExport.toDailyTask(): DailyTask {
    return DailyTask(
        id = id,
        name = name,
        description = description,
        rewardCoins = rewardCoins,
        rewardXP = rewardXP,
        isCompleted = isCompleted
    )
}

private fun SettingsExport.toSettings(): Settings {
    return Settings(
        notificationsEnabled = notificationsEnabled,
        reminderTimes = reminderTimes
    )
}

