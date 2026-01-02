package com.charles.virtualpet.fishtank.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupEnvelope(
    val schemaVersion: Int = 1,
    val exportedAtEpoch: Long,
    val appId: String = "pixel-fish-tank",
    val data: GameStateExport
)

