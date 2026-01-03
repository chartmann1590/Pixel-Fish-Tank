package com.charles.virtualpet.fishtank.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.model.Decoration
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.domain.model.InventoryItem
import com.charles.virtualpet.fishtank.domain.model.ItemType
import com.charles.virtualpet.fishtank.domain.model.PlacedDecoration
import com.charles.virtualpet.fishtank.widgets.WidgetUpdateHelper
import com.charles.virtualpet.fishtank.notifications.NotificationScheduler
import com.charles.virtualpet.fishtank.notifications.PersistentNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GameViewModel(
    application: Application,
    private val repository: GameStateRepository
) : AndroidViewModel(application) {

    private val notificationScheduler = NotificationScheduler(application)
    private val persistentNotificationManager = PersistentNotificationManager(application)

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
        .map { it ?: GameState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameState()
        )

    private var isInitialLoad = true

    init {
        // Load initial state from repository and apply decay
        viewModelScope.launch {
            repository.gameState.collect { state ->
                if (isInitialLoad) {
                    // Apply decay on first load only
                    val decayedState = state.copy(
                        fishState = StatDecayCalculator.calculateDecay(state.fishState),
                        dailyTasks = DailyTaskManager.resetTasksIfNeeded(state.dailyTasks)
                    )
                    _gameState.value = decayedState
                    // Save the decayed state to persist the updated timestamp
                    saveState(decayedState)
                    // Update persistent notification
                    persistentNotificationManager.updateNotification(decayedState)
                    isInitialLoad = false
                } else {
                    // On subsequent emissions (from our own saves), just update state
                    _gameState.value = state
                    // Update persistent notification when state changes
                    persistentNotificationManager.updateNotification(state)
                }
            }
        }
    }

    private fun saveState(gameState: GameState) {
        viewModelScope.launch {
            repository.saveGameState(gameState)
        }
    }

    /**
     * Calculate the XP required to reach a given level.
     * Uses a progressive formula: XP = 100 * level * (level + 1) / 2
     * This ensures each level requires progressively more XP:
     * - Level 1: 100 XP
     * - Level 2: 300 XP (200 more)
     * - Level 3: 600 XP (300 more)
     * - Level 4: 1000 XP (400 more)
     * - Level 5: 1500 XP (500 more)
     * And so on, with unlimited levels.
     */
    private fun getXPRequiredForLevel(level: Int): Int {
        return 100 * level * (level + 1) / 2
    }

    /**
     * Calculate the new level based on current XP and level.
     * Handles multiple level-ups if enough XP is gained.
     * Supports unlimited levels.
     */
    private fun calculateNewLevel(currentLevel: Int, currentXP: Int, xpGained: Int): Int {
        var newXP = currentXP + xpGained
        var newLevel = currentLevel
        
        // Keep leveling up until we don't have enough XP for the next level
        while (newXP >= getXPRequiredForLevel(newLevel + 1)) {
            newLevel++
        }
        
        return newLevel
    }

    fun feedFish() {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            // Apply decay before action
            val decayedFish = StatDecayCalculator.calculateDecay(state.fishState)
            val newHunger = (decayedFish.hunger + 30f).coerceAtMost(100f)
            val newHappiness = (decayedFish.happiness + 5f).coerceAtMost(100f)
            
            // Complete feed task if not already completed and get rewards
            val taskResult = completeTaskIfNotDone(state.dailyTasks, "feed_fish")
            
            // Apply rewards directly in this update block to avoid nested updates
            val currentXP = state.fishState.xp
            val currentLevel = state.fishState.level
            val newXP = currentXP + taskResult.rewardXP
            val newLevel = calculateNewLevel(currentLevel, currentXP, taskResult.rewardXP)
            
            val updatedState = state.copy(
                fishState = decayedFish.copy(
                    hunger = newHunger,
                    happiness = newHappiness,
                    xp = newXP,
                    level = newLevel,
                    lastUpdatedEpoch = System.currentTimeMillis()
                ),
                economy = state.economy.copy(
                    coins = state.economy.coins + taskResult.rewardCoins
                ),
                dailyTasks = taskResult.tasks
            )
            saveState(updatedState)
            // Update widgets after state change
            viewModelScope.launch {
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            }
            updatedState
        }
    }

    fun cleanTank() {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            // Apply decay before action
            val decayedFish = StatDecayCalculator.calculateDecay(state.fishState)
            val newHappiness = (decayedFish.happiness + 10f).coerceAtMost(100f)
            
            // Complete clean task if not already completed and get rewards
            val taskResult = completeTaskIfNotDone(state.dailyTasks, "clean_tank")
            
            // Apply rewards directly in this update block to avoid nested updates
            val currentXP = state.fishState.xp
            val currentLevel = state.fishState.level
            val newXP = currentXP + taskResult.rewardXP
            val newLevel = calculateNewLevel(currentLevel, currentXP, taskResult.rewardXP)
            
            val updatedState = state.copy(
                fishState = decayedFish.copy(
                    cleanliness = 100f,
                    happiness = newHappiness,
                    xp = newXP,
                    level = newLevel,
                    lastUpdatedEpoch = System.currentTimeMillis()
                ),
                economy = state.economy.copy(
                    coins = state.economy.coins + taskResult.rewardCoins
                ),
                dailyTasks = taskResult.tasks
            )
            saveState(updatedState)
            // Update widgets after state change
            viewModelScope.launch {
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            }
            // Update persistent notification
            persistentNotificationManager.updateNotification(updatedState)
            updatedState
        }
    }

    fun addCoins(amount: Int) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                economy = state.economy.copy(
                    coins = state.economy.coins + amount
                )
            )
            saveState(updatedState)
            // Update widgets after state change (minigame completion)
            viewModelScope.launch {
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            }
            updatedState
        }
    }

    fun increaseHappiness(amount: Float) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            // Apply decay before action
            val decayedFish = StatDecayCalculator.calculateDecay(state.fishState)
            val newHappiness = (decayedFish.happiness + amount).coerceAtMost(100f)
            
            val updatedState = state.copy(
                fishState = decayedFish.copy(
                    happiness = newHappiness,
                    lastUpdatedEpoch = System.currentTimeMillis()
                )
            )
            saveState(updatedState)
            // Update widgets after state change
            viewModelScope.launch {
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            }
            // Update persistent notification
            persistentNotificationManager.updateNotification(updatedState)
            updatedState
        }
    }

    fun addXP(amount: Int) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val currentXP = state.fishState.xp
            val currentLevel = state.fishState.level
            val newXP = currentXP + amount
            
            // Calculate new level with progressive XP requirements
            val newLevel = calculateNewLevel(currentLevel, currentXP, amount)
            
            val updatedState = state.copy(
                fishState = state.fishState.copy(
                    xp = newXP,
                    level = newLevel
                )
            )
            saveState(updatedState)
            // Update widgets after state change (minigame completion)
            viewModelScope.launch {
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            }
            updatedState
        }
    }

    fun purchaseDecoration(decoration: Decoration) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val currentCoins = state.economy.coins
            
            if (currentCoins >= decoration.price) {
                val existingItem = state.economy.inventoryItems.find { it.id == decoration.id }
                val newInventory = if (existingItem != null) {
                    // Increment quantity if already owned
                    state.economy.inventoryItems.map { item ->
                        if (item.id == decoration.id) {
                            item.copy(quantity = item.quantity + 1)
                        } else {
                            item
                        }
                    }
                } else {
                    // Add new item with quantity 1
                    state.economy.inventoryItems + InventoryItem(
                        id = decoration.id,
                        name = decoration.name,
                        type = ItemType.DECORATION,
                        quantity = 1
                    )
                }
                val updatedState = state.copy(
                    economy = state.economy.copy(
                        coins = currentCoins - decoration.price,
                        inventoryItems = newInventory
                    )
                )
                saveState(updatedState)
                
                // Track purchase in Firestore
                trackPurchase(decoration)
                
                updatedState
            } else {
                state
            }
        }
    }
    
    private fun trackPurchase(decoration: Decoration) {
        viewModelScope.launch {
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val context = getApplication<android.app.Application>()
                val androidId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: "unknown"
                
                val purchaseData = hashMapOf(
                    "itemId" to decoration.id,
                    "itemName" to decoration.name,
                    "price" to decoration.price,
                    "type" to decoration.type.name,
                    "timestamp" to System.currentTimeMillis(),
                    "userId" to androidId
                )
                firestore.collection("purchases").add(purchaseData).await()
            } catch (e: Exception) {
                // Silently fail - purchase tracking is not critical
                android.util.Log.w("GameViewModel", "Failed to track purchase", e)
            }
        }
    }

    fun placeDecoration(decorationId: String, x: Float, y: Float) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            
            // Check if we have available quantity
            val inventoryItem = state.economy.inventoryItems.find { it.id == decorationId }
            if (inventoryItem == null || inventoryItem.quantity <= 0) {
                return@update state // Can't place, no quantity available
            }
            
            // Create placed decoration with unique ID
            val placedId = java.util.UUID.randomUUID().toString()
            val newPlacedDecoration = PlacedDecoration(
                id = placedId,
                decorationId = decorationId,
                x = x.coerceIn(0f, 1f),
                y = y.coerceIn(0f, 1f)
            )
            
            // Decrement quantity
            val updatedInventory = state.economy.inventoryItems.map { item ->
                if (item.id == decorationId) {
                    item.copy(quantity = item.quantity - 1)
                } else {
                    item
                }
            }
            
            val updatedLayout = state.tankLayout.copy(
                placedDecorations = state.tankLayout.placedDecorations + newPlacedDecoration
            )
            val updatedState = state.copy(
                tankLayout = updatedLayout,
                economy = state.economy.copy(inventoryItems = updatedInventory)
            )
            saveState(updatedState)
            updatedState
        }
    }

    fun removeDecoration(placedDecorationId: String) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            
            // Find the placed decoration to remove
            val placedDecoration = state.tankLayout.placedDecorations.find { it.id == placedDecorationId }
            if (placedDecoration == null) {
                return@update state
            }
            
            // Remove from placed decorations
            val updatedLayout = state.tankLayout.copy(
                placedDecorations = state.tankLayout.placedDecorations.filter { 
                    it.id != placedDecorationId 
                }
            )
            
            // Increment quantity back to inventory
            val updatedInventory = state.economy.inventoryItems.map { item ->
                if (item.id == placedDecoration.decorationId) {
                    item.copy(quantity = item.quantity + 1)
                } else {
                    item
                }
            }
            
            val updatedState = state.copy(
                tankLayout = updatedLayout,
                economy = state.economy.copy(inventoryItems = updatedInventory)
            )
            saveState(updatedState)
            updatedState
        }
    }

    fun completeTask(taskId: String) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val taskResult = completeTaskIfNotDone(state.dailyTasks, taskId)
            
            // Apply rewards directly in this update block to avoid nested updates
            val currentXP = state.fishState.xp
            val currentLevel = state.fishState.level
            val newXP = currentXP + taskResult.rewardXP
            val newLevel = calculateNewLevel(currentLevel, currentXP, taskResult.rewardXP)
            
            val updatedState = state.copy(
                fishState = state.fishState.copy(
                    xp = newXP,
                    level = newLevel
                ),
                economy = state.economy.copy(
                    coins = state.economy.coins + taskResult.rewardCoins
                ),
                dailyTasks = taskResult.tasks
            )
            saveState(updatedState)
            updatedState
        }
    }

    fun completeMinigameTask() {
        completeTask("play_minigame")
    }

    fun completeDecorateTask() {
        completeTask("decorate_tank")
    }

    // Data class to return task completion result with rewards
    private data class TaskCompletionResult(
        val tasks: com.charles.virtualpet.fishtank.domain.model.DailyTasksState,
        val rewardCoins: Int,
        val rewardXP: Int
    )
    
    private fun completeTaskIfNotDone(
        currentTasks: com.charles.virtualpet.fishtank.domain.model.DailyTasksState,
        taskId: String
    ): TaskCompletionResult {
        val task = currentTasks.tasks.find { it.id == taskId }
        if (task != null && !task.isCompleted) {
            val updatedTasksState = DailyTaskManager.completeTask(taskId, currentTasks)
            // Return rewards to be applied in the same update block (no nested updates)
            return TaskCompletionResult(
                tasks = updatedTasksState,
                rewardCoins = task.rewardCoins,
                rewardXP = task.rewardXP
            )
        }
        return TaskCompletionResult(
            tasks = currentTasks,
            rewardCoins = 0,
            rewardXP = 0
        )
    }

    fun updateNotificationSettings(enabled: Boolean, reminderTimes: List<String>) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    notificationsEnabled = enabled,
                    reminderTimes = reminderTimes,
                    // When enabling notifications, enable daily reminder by default if not already set
                    dailyReminderEnabled = if (enabled && !state.settings.notificationsEnabled) {
                        true
                    } else {
                        state.settings.dailyReminderEnabled
                    }
                )
            )
            saveState(updatedState)
            // Schedule/cancel work based on new settings
            scheduleNotificationWork(updatedState.settings)
            updatedState
        }
    }

    fun updateDailyReminderSettings(enabled: Boolean, time: String) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    dailyReminderEnabled = enabled,
                    dailyReminderTime = time
                )
            )
            saveState(updatedState)
            // Reschedule work
            scheduleNotificationWork(updatedState.settings)
            updatedState
        }
    }

    fun updateStatusNudgesSettings(enabled: Boolean) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    statusNudgesEnabled = enabled
                )
            )
            saveState(updatedState)
            // Reschedule work
            scheduleNotificationWork(updatedState.settings)
            updatedState
        }
    }

    fun updatePersistentNotificationSettings(enabled: Boolean) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    persistentNotificationEnabled = enabled
                )
            )
            saveState(updatedState)
            // Update or cancel persistent notification
            if (enabled) {
                persistentNotificationManager.updateNotification(updatedState)
            } else {
                persistentNotificationManager.cancelNotification()
            }
            updatedState
        }
    }

    fun updateQuietHoursSettings(enabled: Boolean, start: String, end: String) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    quietHoursEnabled = enabled,
                    quietHoursStart = start,
                    quietHoursEnd = end
                )
            )
            saveState(updatedState)
            // Quiet hours affect when notifications are sent, but don't require rescheduling
            updatedState
        }
    }

    private fun scheduleNotificationWork(settings: com.charles.virtualpet.fishtank.domain.model.Settings) {
        notificationScheduler.scheduleAllWork(
            notificationsEnabled = settings.notificationsEnabled,
            dailyReminderEnabled = settings.dailyReminderEnabled,
            dailyReminderTime = settings.dailyReminderTime,
            statusNudgesEnabled = settings.statusNudgesEnabled
        )
    }

    fun updateSfxSettings(enabled: Boolean) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    sfxEnabled = enabled
                )
            )
            saveState(updatedState)
            updatedState
        }
    }

    fun updateBgMusicSettings(enabled: Boolean) {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    bgMusicEnabled = enabled
                )
            )
            saveState(updatedState)
            updatedState
        }
    }

    fun completeTutorial() {
        _gameState.update { currentState ->
            val state = currentState ?: GameState()
            val updatedState = state.copy(
                settings = state.settings.copy(
                    hasCompletedTutorial = true
                )
            )
            saveState(updatedState)
            updatedState
        }
    }
}


