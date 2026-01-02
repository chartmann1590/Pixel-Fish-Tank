package com.charles.virtualpet.fishtank.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charles.virtualpet.fishtank.domain.model.Economy
import com.charles.virtualpet.fishtank.domain.model.FishState
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.domain.model.InventoryItem
import com.charles.virtualpet.fishtank.domain.model.ItemType
import com.charles.virtualpet.fishtank.domain.model.DailyTask
import com.charles.virtualpet.fishtank.domain.model.DailyTasksState
import com.charles.virtualpet.fishtank.domain.model.PlacedDecoration
import com.charles.virtualpet.fishtank.domain.model.Settings
import com.charles.virtualpet.fishtank.domain.model.TankLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_state")

class GameStateRepository(private val context: Context) {

    // Keys for DataStore
    private object Keys {
        val HUNGER = floatPreferencesKey("hunger")
        val CLEANLINESS = floatPreferencesKey("cleanliness")
        val HAPPINESS = floatPreferencesKey("happiness")
        val LEVEL = intPreferencesKey("level")
        val XP = intPreferencesKey("xp")
        val LAST_UPDATED_EPOCH = longPreferencesKey("last_updated_epoch")
        val COINS = intPreferencesKey("coins")
        val INVENTORY_ITEMS = stringPreferencesKey("inventory_items")
        val MINIGAME_HIGH_SCORE = intPreferencesKey("minigame_high_score")
        val TANK_LAYOUT = stringPreferencesKey("tank_layout")
        val DAILY_TASKS = stringPreferencesKey("daily_tasks")
        val LAST_RESET_DATE = stringPreferencesKey("last_reset_date")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_COMPLETED_DATE = stringPreferencesKey("last_completed_date")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_TIMES = stringPreferencesKey("reminder_times")
    }

    val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        GameState(
            fishState = FishState(
                hunger = preferences[Keys.HUNGER] ?: 50f,
                cleanliness = preferences[Keys.CLEANLINESS] ?: 100f,
                happiness = preferences[Keys.HAPPINESS] ?: 50f,
                level = preferences[Keys.LEVEL] ?: 1,
                xp = preferences[Keys.XP] ?: 0,
                lastUpdatedEpoch = preferences[Keys.LAST_UPDATED_EPOCH] ?: System.currentTimeMillis()
            ),
            economy = Economy(
                coins = preferences[Keys.COINS] ?: 0,
                inventoryItems = parseInventoryItems(preferences[Keys.INVENTORY_ITEMS])
            ),
            tankLayout = parseTankLayout(preferences[Keys.TANK_LAYOUT]),
            dailyTasks = parseDailyTasksState(preferences),
            settings = parseSettings(preferences)
        )
    }

    suspend fun saveGameState(gameState: GameState) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HUNGER] = gameState.fishState.hunger
            preferences[Keys.CLEANLINESS] = gameState.fishState.cleanliness
            preferences[Keys.HAPPINESS] = gameState.fishState.happiness
            preferences[Keys.LEVEL] = gameState.fishState.level
            preferences[Keys.XP] = gameState.fishState.xp
            preferences[Keys.LAST_UPDATED_EPOCH] = gameState.fishState.lastUpdatedEpoch
            preferences[Keys.COINS] = gameState.economy.coins
            preferences[Keys.INVENTORY_ITEMS] = serializeInventoryItems(gameState.economy.inventoryItems)
            preferences[Keys.TANK_LAYOUT] = serializeTankLayout(gameState.tankLayout)
            
            // Save daily tasks
            preferences[Keys.LAST_RESET_DATE] = gameState.dailyTasks.lastResetDate
            preferences[Keys.CURRENT_STREAK] = gameState.dailyTasks.currentStreak
            preferences[Keys.LONGEST_STREAK] = gameState.dailyTasks.longestStreak
            preferences[Keys.LAST_COMPLETED_DATE] = gameState.dailyTasks.lastCompletedDate
            preferences[Keys.DAILY_TASKS] = serializeDailyTasks(gameState.dailyTasks.tasks)
            
            // Save settings
            preferences[Keys.NOTIFICATIONS_ENABLED] = gameState.settings.notificationsEnabled
            preferences[Keys.REMINDER_TIMES] = serializeReminderTimes(gameState.settings.reminderTimes)
        }
    }

    val highScore: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.MINIGAME_HIGH_SCORE] ?: 0
    }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[Keys.MINIGAME_HIGH_SCORE] ?: 0
            if (score > currentHigh) {
                preferences[Keys.MINIGAME_HIGH_SCORE] = score
            }
        }
    }

    private fun serializeInventoryItems(items: List<InventoryItem>): String {
        if (items.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject()
            jsonObject.put("id", item.id)
            jsonObject.put("name", item.name)
            jsonObject.put("type", item.type.name)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseInventoryItems(jsonString: String?): List<InventoryItem> {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { i ->
                val jsonObject = jsonArray.getJSONObject(i)
                InventoryItem(
                    id = jsonObject.getString("id"),
                    name = jsonObject.getString("name"),
                    type = ItemType.valueOf(jsonObject.getString("type"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeTankLayout(layout: TankLayout): String {
        if (layout.placedDecorations.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        layout.placedDecorations.forEach { placed ->
            val jsonObject = JSONObject()
            jsonObject.put("decorationId", placed.decorationId)
            jsonObject.put("x", placed.x.toDouble())
            jsonObject.put("y", placed.y.toDouble())
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseTankLayout(jsonString: String?): TankLayout {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return TankLayout()
        return try {
            val jsonArray = JSONArray(jsonString)
            val placedDecorations = (0 until jsonArray.length()).map { i ->
                val jsonObject = jsonArray.getJSONObject(i)
                PlacedDecoration(
                    decorationId = jsonObject.getString("decorationId"),
                    x = jsonObject.getDouble("x").toFloat(),
                    y = jsonObject.getDouble("y").toFloat()
                )
            }
            TankLayout(placedDecorations = placedDecorations)
        } catch (e: Exception) {
            TankLayout()
        }
    }

    private fun serializeDailyTasks(tasks: List<DailyTask>): String {
        if (tasks.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        tasks.forEach { task ->
            val jsonObject = JSONObject()
            jsonObject.put("id", task.id)
            jsonObject.put("name", task.name)
            jsonObject.put("description", task.description)
            jsonObject.put("rewardCoins", task.rewardCoins)
            jsonObject.put("rewardXP", task.rewardXP)
            jsonObject.put("isCompleted", task.isCompleted)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseDailyTasksState(preferences: Preferences): DailyTasksState {
        val lastResetDate = preferences[Keys.LAST_RESET_DATE] ?: ""
        val currentStreak = preferences[Keys.CURRENT_STREAK] ?: 0
        val longestStreak = preferences[Keys.LONGEST_STREAK] ?: 0
        val lastCompletedDate = preferences[Keys.LAST_COMPLETED_DATE] ?: ""
        
        val tasksJson = preferences[Keys.DAILY_TASKS]
        val tasks = if (tasksJson.isNullOrBlank() || tasksJson == "[]") {
            emptyList()
        } else {
            try {
                val jsonArray = JSONArray(tasksJson)
                (0 until jsonArray.length()).map { i ->
                    val jsonObject = jsonArray.getJSONObject(i)
                    DailyTask(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        description = jsonObject.getString("description"),
                        rewardCoins = jsonObject.getInt("rewardCoins"),
                        rewardXP = jsonObject.getInt("rewardXP"),
                        isCompleted = jsonObject.getBoolean("isCompleted")
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        return DailyTasksState(
            tasks = tasks,
            lastResetDate = lastResetDate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompletedDate
        )
    }

    private fun parseSettings(preferences: Preferences): Settings {
        return Settings(
            notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: false,
            reminderTimes = parseReminderTimes(preferences[Keys.REMINDER_TIMES])
        )
    }

    private fun serializeReminderTimes(times: List<String>): String {
        if (times.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        times.forEach { time ->
            jsonArray.put(time)
        }
        return jsonArray.toString()
    }

    private fun parseReminderTimes(jsonString: String?): List<String> {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { i ->
                jsonArray.getString(i)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

