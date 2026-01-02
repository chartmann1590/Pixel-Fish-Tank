package com.charles.virtualpet.fishtank.domain

import com.charles.virtualpet.fishtank.domain.model.FishMood
import com.charles.virtualpet.fishtank.domain.model.FishState

object MoodCalculator {
    // Thresholds for mood determination
    private const val HAPPY_HUNGER_THRESHOLD = 70f
    private const val HAPPY_CLEANLINESS_THRESHOLD = 70f
    private const val HAPPY_HAPPINESS_THRESHOLD = 70f
    
    private const val HUNGRY_THRESHOLD = 30f
    private const val DIRTY_THRESHOLD = 30f
    private const val SAD_HAPPINESS_THRESHOLD = 20f

    /**
     * Calculates the fish's mood based on current stats.
     * Priority: SAD > HUNGRY > DIRTY > HAPPY > NEUTRAL
     */
    fun calculateMood(fishState: FishState): FishMood {
        val hunger = fishState.hunger
        val cleanliness = fishState.cleanliness
        val happiness = fishState.happiness

        // Sad: Very low happiness or multiple stats critically low
        if (happiness <= SAD_HAPPINESS_THRESHOLD || 
            (hunger <= HUNGRY_THRESHOLD && cleanliness <= DIRTY_THRESHOLD)) {
            return FishMood.SAD
        }

        // Hungry: Hunger is critically low
        if (hunger <= HUNGRY_THRESHOLD) {
            return FishMood.HUNGRY
        }

        // Dirty: Cleanliness is critically low
        if (cleanliness <= DIRTY_THRESHOLD) {
            return FishMood.DIRTY
        }

        // Happy: All stats are in good range
        if (hunger >= HAPPY_HUNGER_THRESHOLD &&
            cleanliness >= HAPPY_CLEANLINESS_THRESHOLD &&
            happiness >= HAPPY_HAPPINESS_THRESHOLD) {
            return FishMood.HAPPY
        }

        // Neutral: Default state (stats are okay but not great)
        return FishMood.NEUTRAL
    }
}

