package com.charles.virtualpet.fishtank.playgames

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.os.Build
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.analytics.AnalyticsHelper
import com.charles.virtualpet.fishtank.backup.BackupEnvelope
import com.charles.virtualpet.fishtank.backup.BackupRepository
import com.charles.virtualpet.fishtank.backup.BackupSerializer
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.max

internal enum class CloudSaveState {
    IDLE,
    SYNCING,
    SAVED,
    RESTORED,
    ERROR
}

internal object CloudSavePolicy {
    fun progress(level: Int, xp: Int): Long = level.toLong() * 1_000_000L + xp

    fun shouldRestoreCloud(cloudLevel: Int, cloudXp: Int, localLevel: Int, localXp: Int): Boolean =
        progress(cloudLevel, cloudXp) > progress(localLevel, localXp)
}

/** One-slot automatic Play Games Saved Games implementation. */
internal class PlayGamesCloudSave(
    private val activity: Activity,
    private val repository: GameStateRepository,
    private val onStateChanged: (CloudSaveState, String) -> Unit
) {
    private val backupRepository = BackupRepository(activity.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var autosaveJob: Job? = null
    private var latestState: GameState? = null
    private var initialSyncComplete = false
    private val sessionStartedAt = System.currentTimeMillis()
    private val metadataPrefs = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)

    fun onAuthenticated() {
        if (initialSyncComplete) return
        scope.launch { synchronizeAtSignIn() }
    }

    fun observe(state: GameState) {
        latestState = state
        if (!initialSyncComplete) return
        autosaveJob?.cancel()
        autosaveJob = scope.launch {
            delay(AUTOSAVE_DELAY_MS)
            save(state, automatic = true)
        }
    }

    fun saveNow() {
        val state = latestState ?: return
        scope.launch { save(state, automatic = false) }
    }

    fun restoreNow() {
        scope.launch { restore() }
    }

    @Suppress("DEPRECATION")
    fun showSelector() {
        PlayGames.getSnapshotsClient(activity)
            .getSelectSnapshotIntent("Pixel Fish Tank cloud save", false, true, 1)
            .addOnSuccessListener { intent -> activity.startActivityForResult(intent, REQUEST_SAVED_GAMES) }
            .addOnFailureListener { fail("Could not open cloud saves", it) }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_SAVED_GAMES) return false
        if (resultCode != Activity.RESULT_OK || data == null) return true
        val metadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA, SnapshotMetadata::class.java)
        } else {
            @Suppress("DEPRECATION")
            data.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)
        }
        metadata?.uniqueName?.let { selectedName -> scope.launch { restore(selectedName) } }
        return true
    }

    fun flush() {
        if (!initialSyncComplete) return
        latestState?.let { state -> scope.launch { save(state, automatic = true) } }
    }

    fun close() {
        scope.cancel()
    }

    private suspend fun synchronizeAtSignIn() {
        update(CloudSaveState.SYNCING, "Checking cloud save…")
        runCatching {
            val local = repository.gameState.first()
            latestState = local
            val snapshot = openSnapshot()
            val bytes = withContext(Dispatchers.IO) { snapshot.snapshotContents.readFully() }
            if (bytes.isEmpty()) {
                commit(snapshot, backupRepository.createCloudSaveEnvelope(local, repository))
                update(CloudSaveState.SAVED, "Cloud save created")
            } else {
                val cloud = BackupSerializer.deserialize(bytes.toString(Charsets.UTF_8))
                if (CloudSavePolicy.shouldRestoreCloud(
                        cloud.data.fishState.level,
                        cloud.data.fishState.xp,
                        local.fishState.level,
                        local.fishState.xp
                    )
                ) {
                    backupRepository.importState(cloud, repository).getOrThrow()
                    latestState = repository.gameState.first()
                    update(CloudSaveState.RESTORED, "Progress restored from Play Games")
                    AnalyticsHelper.logCloudSave("restore", true, automatic = true)
                } else {
                    commit(snapshot, backupRepository.createCloudSaveEnvelope(local, repository))
                    update(CloudSaveState.SAVED, "Cloud save is up to date")
                }
            }
            initialSyncComplete = true
        }.onFailure { fail("Cloud save sync failed", it) }
    }

    private suspend fun save(state: GameState, automatic: Boolean) {
        update(CloudSaveState.SYNCING, if (automatic) "Syncing progress…" else "Saving to cloud…")
        runCatching {
            val snapshot = openSnapshot()
            val envelope = backupRepository.createCloudSaveEnvelope(state, repository)
            commit(snapshot, envelope)
            update(CloudSaveState.SAVED, "Saved to Play Games cloud")
            AnalyticsHelper.logCloudSave("save", true, automatic)
        }.onFailure {
            AnalyticsHelper.logCloudSave("save", false, automatic)
            fail("Cloud save failed", it)
        }
    }

    private suspend fun restore(slotName: String = SLOT_NAME) {
        update(CloudSaveState.SYNCING, "Restoring cloud save…")
        runCatching {
            val snapshot = openSnapshot(slotName)
            val bytes = withContext(Dispatchers.IO) { snapshot.snapshotContents.readFully() }
            require(bytes.isNotEmpty()) { "No cloud save exists yet" }
            val envelope = BackupSerializer.deserialize(bytes.toString(Charsets.UTF_8))
            val local = repository.gameState.first()
            if (CloudSavePolicy.shouldRestoreCloud(
                    envelope.data.fishState.level,
                    envelope.data.fishState.xp,
                    local.fishState.level,
                    local.fishState.xp
                )
            ) {
                backupRepository.importState(envelope, repository).getOrThrow()
                latestState = repository.gameState.first()
                update(CloudSaveState.RESTORED, "Cloud progress restored")
                AnalyticsHelper.logCloudSave("restore", true, automatic = false)
            } else {
                commit(snapshot, backupRepository.createCloudSaveEnvelope(local, repository))
                update(CloudSaveState.SAVED, "Local progress was newer and is safely backed up")
                AnalyticsHelper.logCloudSave("restore_skipped", true, automatic = false)
            }
        }.onFailure {
            AnalyticsHelper.logCloudSave("restore", false, automatic = false)
            fail("Cloud restore failed", it)
        }
    }

    private suspend fun openSnapshot(slotName: String = SLOT_NAME): Snapshot {
        val result = PlayGames.getSnapshotsClient(activity)
            .open(slotName, true, SnapshotsClient.RESOLUTION_POLICY_HIGHEST_PROGRESS)
            .await()
        return requireNotNull(result.data) { "Play Games did not return an open snapshot" }
    }

    private suspend fun commit(snapshot: Snapshot, envelope: BackupEnvelope) {
        val payload = BackupSerializer.serialize(envelope).toByteArray(Charsets.UTF_8)
        require(payload.size <= MAX_SAVE_BYTES) { "Cloud save is larger than 3 MB" }
        withContext(Dispatchers.IO) { snapshot.snapshotContents.writeBytes(payload) }

        val state = envelope.data.fishState
        val metadata = SnapshotMetadataChange.Builder()
            .setDescription("Level ${state.level} fish · ${envelope.data.economy.coins} coins")
            .setProgressValue(CloudSavePolicy.progress(state.level, state.xp))
            .setPlayedTimeMillis(playedTimeMillis())
            .setCoverImage(createCoverImage(state.level))
            .build()
        PlayGames.getSnapshotsClient(activity).commitAndClose(snapshot, metadata).await()
        metadataPrefs.edit().putLong(KEY_PLAYED_TIME, playedTimeMillis()).apply()
    }

    private fun createCoverImage(level: Int): Bitmap {
        val width = 800
        val height = 450
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        BitmapFactory.decodeResource(activity.resources, R.drawable.tank_background)?.let { background ->
            canvas.drawBitmap(background, null, android.graphics.Rect(0, 0, width, height), null)
        }
        BitmapFactory.decodeResource(activity.resources, R.drawable.fish_happy)?.let { fish ->
            val fishWidth = 260
            val fishHeight = 180
            canvas.drawBitmap(
                fish,
                null,
                android.graphics.Rect((width - fishWidth) / 2, 135, (width + fishWidth) / 2, 135 + fishHeight),
                null
            )
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 3f, Color.BLACK)
        }
        canvas.drawText("Pixel Fish Tank · Level $level", width / 2f, 75f, paint)
        return output
    }

    private fun playedTimeMillis(): Long =
        metadataPrefs.getLong(KEY_PLAYED_TIME, 0L) + max(0L, System.currentTimeMillis() - sessionStartedAt)

    private fun update(state: CloudSaveState, message: String) {
        onStateChanged(state, message)
    }

    private fun fail(message: String, error: Throwable) {
        Log.w(TAG, message, error)
        FirebaseCrashlytics.getInstance().recordException(error)
        update(CloudSaveState.ERROR, message)
    }

    private companion object {
        const val TAG = "PlayGamesCloudSave"
        const val SLOT_NAME = "pixel_fish_tank_autosave"
        const val AUTOSAVE_DELAY_MS = 3 * 60 * 1000L
        const val MAX_SAVE_BYTES = 3 * 1024 * 1024
        const val PREFS_NAME = "play_games_cloud_save"
        const val KEY_PLAYED_TIME = "played_time_ms"
        const val REQUEST_SAVED_GAMES = 9009
    }
}
