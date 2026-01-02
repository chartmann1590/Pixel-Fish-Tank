package com.charles.virtualpet.fishtank.domain.model

data class FishState(
    val hunger: Float = 50f,
    val cleanliness: Float = 100f,
    val happiness: Float = 50f,
    val level: Int = 1,
    val xp: Int = 0,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
)

