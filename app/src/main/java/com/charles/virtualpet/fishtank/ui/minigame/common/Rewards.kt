package com.charles.virtualpet.fishtank.ui.minigame.common

object Rewards {
    /**
     * Calculate coins and XP based on score.
     * Coins: score / 10, clamped to 5..50
     * XP: score / 20, clamped to 3..25
     */
    fun calculateRewards(score: Int): Pair<Int, Int> {
        val coins = (score / 10).coerceIn(5, 50)
        val xp = (score / 20).coerceIn(3, 25)
        return Pair(coins, xp)
    }
}

