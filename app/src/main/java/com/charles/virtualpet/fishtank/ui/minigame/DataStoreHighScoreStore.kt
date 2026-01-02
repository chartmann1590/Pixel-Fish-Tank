package com.charles.virtualpet.fishtank.ui.minigame

import com.charles.virtualpet.fishtank.data.GameStateRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DataStoreHighScoreStore(
    private val repository: GameStateRepository
) : HighScoreStore {
    
    override fun get(type: MiniGameType): Int {
        return runBlocking {
            repository.getHighScore(type).first()
        }
    }
    
    override fun set(type: MiniGameType, score: Int) {
        runBlocking {
            repository.saveHighScore(type, score)
        }
    }
}

