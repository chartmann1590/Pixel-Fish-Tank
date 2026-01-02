package com.charles.virtualpet.fishtank.domain

import com.charles.virtualpet.fishtank.domain.model.FishState

object StatDecayCalculator {
    // Decay rates per hour
    private const val HUNGER_DECAY_PER_HOUR = 5f // Hunger decreases by 5 per hour
    private const val CLEANLINESS_DECAY_PER_HOUR = 3f // Cleanliness decreases by 3 per hour
    private const val HAPPINESS_DECAY_PER_HOUR = 2f // Happiness decreases by 2 per hour (if conditions are poor)
    
    // Minimum thresholds for happiness decay
    private const val HAPPINESS_DECAY_HUNGER_THRESHOLD = 30f // If hunger < 30, happiness decays faster
    private const val HAPPINESS_DECAY_CLEANLINESS_THRESHOLD = 30f // If cleanliness < 30, happiness decays faster

    /**
     * Calculates decayed stats based on elapsed time since last update.
     * @param currentState The current fish state
     * @param currentTimeMillis Current time in milliseconds (defaults to System.currentTimeMillis())
     * @return Updated FishState with decayed stats and updated timestamp
     */
    fun calculateDecay(
        currentState: FishState,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): FishState {
        val lastUpdate = currentState.lastUpdatedEpoch
        val elapsedMillis = currentTimeMillis - lastUpdate
        val elapsedHours = elapsedMillis / (1000.0 * 60.0 * 60.0) // Convert to hours
        
        // Don't apply decay if less than 1 minute has passed
        if (elapsedHours < (1.0 / 60.0)) {
            return currentState.copy(lastUpdatedEpoch = currentTimeMillis)
        }

        // Calculate decay amounts
        val hungerDecay = (elapsedHours * HUNGER_DECAY_PER_HOUR).toFloat()
        val cleanlinessDecay = (elapsedHours * CLEANLINESS_DECAY_PER_HOUR).toFloat()
        
        // Apply hunger decay
        val newHunger = (currentState.hunger - hungerDecay).coerceIn(0f, 100f)
        
        // Apply cleanliness decay
        val newCleanliness = (currentState.cleanliness - cleanlinessDecay).coerceIn(0f, 100f)
        
        // Calculate happiness decay (depends on other stats)
        var happinessDecay = (elapsedHours * HAPPINESS_DECAY_PER_HOUR).toFloat()
        
        // Increase happiness decay if hunger or cleanliness is very low
        if (newHunger < HAPPINESS_DECAY_HUNGER_THRESHOLD) {
            happinessDecay += (elapsedHours * 3f).toFloat() // Extra decay when hungry
        }
        if (newCleanliness < HAPPINESS_DECAY_CLEANLINESS_THRESHOLD) {
            happinessDecay += (elapsedHours * 3f).toFloat() // Extra decay when dirty
        }
        
        val newHappiness = (currentState.happiness - happinessDecay).coerceIn(0f, 100f)
        
        return currentState.copy(
            hunger = newHunger,
            cleanliness = newCleanliness,
            happiness = newHappiness,
            lastUpdatedEpoch = currentTimeMillis
        )
    }
}

