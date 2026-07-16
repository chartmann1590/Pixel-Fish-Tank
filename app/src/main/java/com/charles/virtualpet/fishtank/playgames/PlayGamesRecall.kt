package com.charles.virtualpet.fishtank.playgames

import android.app.Activity
import android.util.Log
import com.charles.virtualpet.fishtank.BuildConfig
import com.charles.virtualpet.fishtank.analytics.AnalyticsHelper
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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
    private val httpClient = OkHttpClient()
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

            val recovered = post("v1/recover", JSONObject().put("sessionId", sessionId))
            val customToken = recovered.optString("customToken").takeIf(String::isNotBlank)
            if (!customToken.isNullOrBlank()) {
                Firebase.auth.signInWithCustomToken(customToken).await()
                update(RecallState.RECOVERED, "Account recovered with Play Games Recall")
                AnalyticsHelper.logRecall("recovered", true)
                return
            }

            val idToken = Firebase.auth.currentUser
                ?.getIdToken(false)
                ?.await()
                ?.token
                ?: error("Firebase authentication token is unavailable")
            post(
                "v1/link",
                JSONObject()
                    .put("sessionId", sessionId)
                    .put("idToken", idToken)
            )
            update(RecallState.LINKED, "Account protected by Play Games Recall")
            AnalyticsHelper.logRecall("linked", true)
        }.onFailure { error ->
            Log.w(TAG, "Recall account linking is unavailable", error)
            FirebaseCrashlytics.getInstance().recordException(error)
            update(RecallState.UNAVAILABLE, "Play Games Recall is temporarily unavailable")
            AnalyticsHelper.logRecall("unavailable", false)
        }
    }

    private suspend fun post(path: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.PLAY_GAMES_RECALL_URL.trimEnd('/')
        val request = Request.Builder()
            .url("$baseUrl/$path")
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()
        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("Recall backend returned HTTP ${response.code}")
            }
            JSONObject(responseBody)
        }
    }

    private fun update(state: RecallState, message: String) = onStateChanged(state, message)

    private companion object {
        const val TAG = "PlayGamesRecall"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
