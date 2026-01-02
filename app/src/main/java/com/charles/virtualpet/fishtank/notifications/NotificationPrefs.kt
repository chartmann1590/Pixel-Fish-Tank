package com.charles.virtualpet.fishtank.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.notificationPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

class NotificationPrefs(context: Context) {
    // Ensure we always use application context to avoid memory leaks and null pointer issues
    private val context: Context = context.applicationContext

    private object Keys {
        val LAST_DAILY_REMINDER_EPOCH = longPreferencesKey("last_daily_reminder_epoch")
        val LAST_STATUS_NUDGE_EPOCH_BY_TYPE = stringPreferencesKey("last_status_nudge_epoch_by_type")
        val STATUS_NUDGES_SENT_TODAY_COUNT = intPreferencesKey("status_nudges_sent_today_count")
        val LAST_APP_OPEN_EPOCH = longPreferencesKey("last_app_open_epoch")
        val LAST_DAILY_COUNT_RESET_DATE = stringPreferencesKey("last_daily_count_reset_date")
    }

    val lastDailyReminderEpoch: Flow<Long> = context.notificationPrefsDataStore.data.map { prefs ->
        prefs[Keys.LAST_DAILY_REMINDER_EPOCH] ?: 0L
    }

    val lastStatusNudgeEpochByType: Flow<Map<String, Long>> = context.notificationPrefsDataStore.data.map { prefs ->
        parseStatusNudgeMap(prefs[Keys.LAST_STATUS_NUDGE_EPOCH_BY_TYPE])
    }

    val statusNudgesSentTodayCount: Flow<Int> = context.notificationPrefsDataStore.data.map { prefs ->
        prefs[Keys.STATUS_NUDGES_SENT_TODAY_COUNT] ?: 0
    }

    val lastAppOpenEpoch: Flow<Long> = context.notificationPrefsDataStore.data.map { prefs ->
        prefs[Keys.LAST_APP_OPEN_EPOCH] ?: 0L
    }

    suspend fun updateLastDailyReminderEpoch(epoch: Long) {
        context.notificationPrefsDataStore.edit { prefs ->
            prefs[Keys.LAST_DAILY_REMINDER_EPOCH] = epoch
        }
    }

    suspend fun updateLastStatusNudgeEpoch(type: String, epoch: Long) {
        context.notificationPrefsDataStore.edit { prefs ->
            val currentMap = parseStatusNudgeMap(prefs[Keys.LAST_STATUS_NUDGE_EPOCH_BY_TYPE])
            val updatedMap = currentMap.toMutableMap().apply {
                put(type, epoch)
            }
            prefs[Keys.LAST_STATUS_NUDGE_EPOCH_BY_TYPE] = serializeStatusNudgeMap(updatedMap)
        }
    }

    suspend fun incrementStatusNudgesSentTodayCount() {
        context.notificationPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.STATUS_NUDGES_SENT_TODAY_COUNT] ?: 0
            prefs[Keys.STATUS_NUDGES_SENT_TODAY_COUNT] = current + 1
        }
    }

    suspend fun resetStatusNudgesSentTodayCount() {
        context.notificationPrefsDataStore.edit { prefs ->
            prefs[Keys.STATUS_NUDGES_SENT_TODAY_COUNT] = 0
            // Store today's date to track when we reset
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            prefs[Keys.LAST_DAILY_COUNT_RESET_DATE] = today
        }
    }

    suspend fun updateLastAppOpenEpoch(epoch: Long) {
        context.notificationPrefsDataStore.edit { prefs ->
            prefs[Keys.LAST_APP_OPEN_EPOCH] = epoch
        }
    }

    suspend fun shouldResetDailyCount(): Boolean {
        return context.notificationPrefsDataStore.data.map { prefs ->
            val lastResetDate = prefs[Keys.LAST_DAILY_COUNT_RESET_DATE] ?: ""
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            lastResetDate != today
        }.first()
    }

    private fun parseStatusNudgeMap(jsonString: String?): Map<String, Long> {
        if (jsonString.isNullOrBlank() || jsonString == "{}") return emptyMap()
        return try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<String, Long>()
            json.keys().forEach { key ->
                map[key] = json.getLong(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun serializeStatusNudgeMap(map: Map<String, Long>): String {
        if (map.isEmpty()) return "{}"
        val json = JSONObject()
        map.forEach { (key, value) ->
            json.put(key, value)
        }
        return json.toString()
    }

}

