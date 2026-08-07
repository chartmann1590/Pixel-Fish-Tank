package com.charles.virtualpet.fishtank.review

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

private val Context.reviewPromptDataStore: DataStore<Preferences> by preferencesDataStore(name = "review_prompt_prefs")

private object Keys {
    val LEVEL_UP_COUNT = intPreferencesKey("review_prompt_level_up_count")
    val REQUESTED = booleanPreferencesKey("review_prompt_requested")
}

/** Level-ups celebrated before we ever ask for a review. Early asks convert worse. */
private const val LEVEL_UPS_BEFORE_FIRST_ASK = 2

/**
 * Prompts the official Play In-App Review dialog right after the fish levels up — the game's
 * own moment of delight, not just an app-open. Google's own quota caps how often the dialog can
 * appear regardless of what we request, so this only needs to avoid asking too early and never
 * ask twice.
 */
object ReviewPrompter {
    suspend fun maybeRequestReview(activity: Activity) {
        var shouldRequest = false
        activity.applicationContext.reviewPromptDataStore.edit { prefs ->
            val alreadyRequested = prefs[Keys.REQUESTED] ?: false
            val count = (prefs[Keys.LEVEL_UP_COUNT] ?: 0) + 1
            prefs[Keys.LEVEL_UP_COUNT] = count
            if (!alreadyRequested && count >= LEVEL_UPS_BEFORE_FIRST_ASK) {
                prefs[Keys.REQUESTED] = true
                shouldRequest = true
            }
        }
        if (!shouldRequest) return

        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
        }
    }
}
