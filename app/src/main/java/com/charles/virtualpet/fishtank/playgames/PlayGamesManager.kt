package com.charles.virtualpet.fishtank.playgames

import android.app.Activity
import android.util.Log
import com.charles.virtualpet.fishtank.BuildConfig
import com.charles.virtualpet.fishtank.analytics.AnalyticsHelper
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameResult
import com.charles.virtualpet.fishtank.ui.minigame.MiniGameType
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayGamesStatus(
    val isConfigured: Boolean = BuildConfig.PLAY_GAMES_CONFIGURED,
    val isAuthenticated: Boolean = false,
    val playerName: String? = null,
    val cloudSaveState: String = "idle",
    val cloudSaveMessage: String = "Cloud save is waiting for Play Games",
    val recallState: String = "idle",
    val recallMessage: String = "Recall is waiting for Play Games"
)

/**
 * Owns the Play Games Services v2 integration used by Play Games Sidekick.
 * All operations are best-effort so game play is never blocked by sign-in or
 * network failures.
 */
class PlayGamesManager(
    private val activity: Activity,
    repository: GameStateRepository
) {
    private val _status = MutableStateFlow(PlayGamesStatus())
    val status: StateFlow<PlayGamesStatus> = _status.asStateFlow()

    private val submittedThisSession = mutableSetOf<String>()
    private val pendingMiniGameResults = mutableListOf<MiniGameResult>()
    private val cloudSave = PlayGamesCloudSave(activity, repository) { state, message ->
        _status.value = _status.value.copy(
            cloudSaveState = state.name.lowercase(),
            cloudSaveMessage = message
        )
    }
    private val recall = PlayGamesRecall(activity) { state, message ->
        _status.value = _status.value.copy(
            recallState = state.name.lowercase(),
            recallMessage = message
        )
    }

    fun initialize() {
        if (!BuildConfig.PLAY_GAMES_CONFIGURED) {
            Log.i(TAG, "Play Games Services is disabled: PGS_APP_ID is not configured")
            return
        }
        PlayGamesSdk.initialize(activity.applicationContext)
        refreshAuthentication()
    }

    fun refreshAuthentication() {
        if (!BuildConfig.PLAY_GAMES_CONFIGURED) return
        PlayGames.getGamesSignInClient(activity).isAuthenticated
            .addOnCompleteListener { task ->
                val authenticated = task.isSuccessful && task.result.isAuthenticated
                _status.value = _status.value.copy(isAuthenticated = authenticated)
                if (authenticated) {
                    loadPlayerName()
                    flushPendingMiniGameResults()
                    cloudSave.onAuthenticated()
                    recall.onAuthenticated()
                }
            }
    }

    fun signIn() {
        if (!BuildConfig.PLAY_GAMES_CONFIGURED) return
        PlayGames.getGamesSignInClient(activity).signIn()
            .addOnCompleteListener { refreshAuthentication() }
    }

    fun showAchievements() {
        if (!canUsePlayGames()) return
        PlayGames.getAchievementsClient(activity).achievementsIntent
            .addOnSuccessListener { intent -> activity.startActivity(intent) }
            .addOnFailureListener(::recordFailure)
    }

    fun showLeaderboards() {
        if (!canUsePlayGames()) return
        PlayGames.getLeaderboardsClient(activity).allLeaderboardsIntent
            .addOnSuccessListener { intent -> activity.startActivity(intent) }
            .addOnFailureListener(::recordFailure)
    }

    fun syncMilestones(state: GameState) {
        if (!canUsePlayGames()) return
        cloudSave.observe(state)
        AnalyticsHelper.setFishLevel(state.fishState.level)
        PlayGamesMilestones.achievementsFor(state).forEach { achievement ->
            unlock(achievementId(achievement))
        }
    }

    fun saveCloudNow() {
        if (canUsePlayGames()) cloudSave.saveNow()
    }

    fun restoreCloudNow() {
        if (canUsePlayGames()) cloudSave.restoreNow()
    }

    fun showCloudSaves() {
        if (canUsePlayGames()) cloudSave.showSelector()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?): Boolean =
        cloudSave.handleActivityResult(requestCode, resultCode, data)

    fun flushCloudSave() {
        if (canUsePlayGames()) cloudSave.flush()
    }

    fun close() {
        cloudSave.close()
        recall.close()
    }

    fun recordMiniGame(result: MiniGameResult) {
        if (!BuildConfig.PLAY_GAMES_CONFIGURED) return
        if (!_status.value.isAuthenticated) {
            pendingMiniGameResults += result
            return
        }

        submitMiniGameResult(result)
    }

    private fun submitMiniGameResult(result: MiniGameResult) {

        unlock(BuildConfig.PGS_ACH_FIRST_MINIGAME)
        if (result.isHighScore) unlock(BuildConfig.PGS_ACH_HIGH_SCORE)
        increment(BuildConfig.PGS_ACH_MINIGAME_VETERAN, 1)

        val leaderboardId = when (result.type) {
            MiniGameType.BUBBLE_POP -> BuildConfig.PGS_LB_BUBBLE_POP
            MiniGameType.TIMING_BAR -> BuildConfig.PGS_LB_TIMING_BAR
            MiniGameType.CLEANUP_RUSH -> BuildConfig.PGS_LB_CLEANUP_RUSH
            MiniGameType.FOOD_DROP -> BuildConfig.PGS_LB_FOOD_DROP
            MiniGameType.MEMORY_SHELLS -> BuildConfig.PGS_LB_MEMORY_SHELLS
            MiniGameType.FISH_FOLLOW -> BuildConfig.PGS_LB_FISH_FOLLOW
        }
        if (leaderboardId.isNotBlank()) {
            PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, result.score.toLong())
        }
    }

    private fun achievementId(achievement: PlayGamesAchievement): String = when (achievement) {
        PlayGamesAchievement.FIRST_FEED -> BuildConfig.PGS_ACH_FIRST_FEED
        PlayGamesAchievement.SPARKLING_CLEAN -> BuildConfig.PGS_ACH_SPARKLING_CLEAN
        PlayGamesAchievement.LEVEL_5 -> BuildConfig.PGS_ACH_LEVEL_5
        PlayGamesAchievement.LEVEL_10 -> BuildConfig.PGS_ACH_LEVEL_10
        PlayGamesAchievement.STREAK_3 -> BuildConfig.PGS_ACH_STREAK_3
        PlayGamesAchievement.STREAK_7 -> BuildConfig.PGS_ACH_STREAK_7
        PlayGamesAchievement.DECORATOR -> BuildConfig.PGS_ACH_DECORATOR
        PlayGamesAchievement.COLLECTOR -> BuildConfig.PGS_ACH_COLLECTOR
    }

    private fun unlock(id: String) {
        if (id.isBlank() || !submittedThisSession.add("unlock:$id")) return
        PlayGames.getAchievementsClient(activity).unlock(id)
    }

    private fun increment(id: String, steps: Int) {
        if (id.isBlank()) return
        PlayGames.getAchievementsClient(activity).increment(id, steps)
    }

    private fun loadPlayerName() {
        PlayGames.getPlayersClient(activity).currentPlayer
            .addOnSuccessListener { player ->
                _status.value = _status.value.copy(playerName = player.displayName)
                AnalyticsHelper.setPlayGamesPlayer(player.playerId)
            }
            .addOnFailureListener(::recordFailure)
    }

    private fun flushPendingMiniGameResults() {
        if (pendingMiniGameResults.isEmpty()) return
        val results = pendingMiniGameResults.toList()
        pendingMiniGameResults.clear()
        results.forEach(::submitMiniGameResult)
    }

    private fun canUsePlayGames(): Boolean =
        BuildConfig.PLAY_GAMES_CONFIGURED && _status.value.isAuthenticated

    private fun recordFailure(error: Exception) {
        Log.w(TAG, "Play Games Services operation failed", error)
        FirebaseCrashlytics.getInstance().recordException(error)
    }

    private companion object {
        const val TAG = "PlayGamesManager"
    }
}
