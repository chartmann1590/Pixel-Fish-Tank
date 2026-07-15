package com.charles.virtualpet.fishtank.data.feedback

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.bugReportDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "feedback_bug_reports"
)

class BugReportRepo(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    companion object {
        private val BUG_REPORTS_KEY = stringPreferencesKey("bug_reports_list")
    }

    val bugReports: Flow<List<BugReport>> = context.bugReportDataStore.data.map { prefs ->
        val stored = prefs[BUG_REPORTS_KEY] ?: "[]"
        try {
            json.decodeFromString<List<BugReport>>(stored)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveBugReport(report: BugReport) {
        context.bugReportDataStore.edit { prefs ->
            val current = try {
                json.decodeFromString<List<BugReport>>(prefs[BUG_REPORTS_KEY] ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            val updated = current.toMutableList()
            val existingIndex = updated.indexOfFirst { it.number == report.number }
            if (existingIndex >= 0) {
                updated[existingIndex] = report
            } else {
                updated.add(report)
            }
            prefs[BUG_REPORTS_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun updateBugReports(reports: List<BugReport>) {
        context.bugReportDataStore.edit { prefs ->
            prefs[BUG_REPORTS_KEY] = json.encodeToString(reports)
        }
    }

    suspend fun getBugReportsList(): List<BugReport> {
        val prefs = context.bugReportDataStore.data
        var result: List<BugReport> = emptyList()
        prefs.collect { stored ->
            result = try {
                json.decodeFromString<List<BugReport>>(stored[BUG_REPORTS_KEY] ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            return@collect
        }
        return result
    }
}
