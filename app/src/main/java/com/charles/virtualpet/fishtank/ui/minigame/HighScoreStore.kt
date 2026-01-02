package com.charles.virtualpet.fishtank.ui.minigame

interface HighScoreStore {
    fun get(type: MiniGameType): Int
    fun set(type: MiniGameType, score: Int)
}

class InMemoryHighScoreStore : HighScoreStore {
    private val scores = mutableMapOf<MiniGameType, Int>()
    
    override fun get(type: MiniGameType): Int {
        return scores[type] ?: 0
    }
    
    override fun set(type: MiniGameType, score: Int) {
        val currentHigh = scores[type] ?: 0
        if (score > currentHigh) {
            scores[type] = score
        }
    }
}

