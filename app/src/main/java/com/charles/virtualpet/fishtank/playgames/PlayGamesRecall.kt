package com.charles.virtualpet.fishtank.playgames

import android.app.Activity
import android.util.Log
import com.charles.virtualpet.fishtank.analytics.AnalyticsHelper
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

internal enum class RecallState {
    IDLE,
    LINKING,
    LINKED,
    RECOVERED,
    UNAVAILABLE
}

/**
 * Links the Play Games profile to the app's anonymous Firebase account. On a
 * fresh installation the backend can recover that Firebase identity and issue
 * a custom sign-in token, giving Recall a concrete account-recovery purpose.
 */
internal class PlayGamesRecall(
    private val activity: Activity,
    private val onStateChanged: (RecallState, String) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var attempted = false

    fun onAuthenticated() {
        if (attempted) return
        attempted = true
        scope.launch { linkOrRecover() }
    }

    fun close() = scope.cancel()

    private suspend fun linkOrRecover() {
        update(RecallState.LINKING, "Linking Play Games account…")
        runCatching {
            if (Firebase.auth.currentUser == null) {
                Firebase.auth.signInAnonymously().await()
            }

            val sessionId = PlayGames.getRecallClient(activity)
                .requestRecallAccess()
                .await()
                .sessionId

            val recovered = Firebase.functions
                .getHttpsCallable("recallRecover")
                .call(mapOf("sessionId" to sessionId))
                .await()
                .data.asStringMap()

            val customToken = recovered["customToken"] as? String
            if (!customToken.isNullOrBlank()) {
                Firebase.auth.signInWithCustomToken(customToken).await()
                update(RecallState.RECOVERED, "Account recovered with Play Games Recall")
                AnalyticsHelper.logRecall("recovered", true)
                return
            }

            Firebase.functions
                .getHttpsCallable("recallLink")
                .call(mapOf("sessionId" to sessionId))
                .await()
            update(RecallState.LINKED, "Account protected by Play Games Recall")
            AnalyticsHelper.logRecall("linked", true)
        }.onFailure { error ->
            Log.w(TAG, "Recall account linking is unavailable", error)
            FirebaseCrashlytics.getInstance().recordException(error)
            update(RecallState.UNAVAILABLE, "Recall will finish when its secure backend is available")
            AnalyticsHelper.logRecall("unavailable", false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Any?.asStringMap(): Map<String, Any?> = this as? Map<String, Any?> ?: emptyMap()

    private fun update(state: RecallState, message: String) = onStateChanged(state, message)

    private companion object {
        const val TAG = "PlayGamesRecall"
    }
}
