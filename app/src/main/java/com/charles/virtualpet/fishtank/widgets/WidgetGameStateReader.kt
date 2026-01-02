package com.charles.virtualpet.fishtank.widgets

import android.content.Context
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.model.GameState
import kotlinx.coroutines.flow.first

/**
 * Read-only access to game state for widgets
 */
class WidgetGameStateReader(context: Context) {
    private val repository = GameStateRepository(context)

    suspend fun readGameState(): GameState {
        return repository.gameState.first()
    }
}
