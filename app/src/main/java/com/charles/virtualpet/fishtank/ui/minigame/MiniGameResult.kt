package com.charles.virtualpet.fishtank.ui.minigame

data class MiniGameResult(
    val type: MiniGameType,
    val score: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val isHighScore: Boolean
)

