package com.charles.virtualpet.fishtank.widgets

import com.charles.virtualpet.fishtank.domain.MoodCalculator
import com.charles.virtualpet.fishtank.domain.StatDecayCalculator
import com.charles.virtualpet.fishtank.domain.model.FishMood
import com.charles.virtualpet.fishtank.domain.model.GameState

/**
 * Derived state for widget display with time decay applied
 */
data class WidgetDerivedState(
    val hunger: Float,
    val cleanliness: Float,
    val happiness: Float,
    val level: Int,
    val xp: Int,
    val coins: Int,
    val mood: FishMood,
    val lastUpdatedEpoch: Long
) {
    companion object {
        fun deriveForDisplay(gameState: GameState): WidgetDerivedState {
            // Apply time decay to get current stats
            val decayedFishState = StatDecayCalculator.calculateDecay(gameState.fishState)

            // Calculate mood based on decayed stats
            val mood = MoodCalculator.calculateMood(decayedFishState)

            return WidgetDerivedState(
                hunger = decayedFishState.hunger,
                cleanliness = decayedFishState.cleanliness,
                happiness = decayedFishState.happiness,
                level = decayedFishState.level,
                xp = decayedFishState.xp,
                coins = gameState.economy.coins,
                mood = mood,
                lastUpdatedEpoch = decayedFishState.lastUpdatedEpoch
            )
        }
    }
}
