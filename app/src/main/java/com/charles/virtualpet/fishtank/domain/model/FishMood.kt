package com.charles.virtualpet.fishtank.domain.model

enum class FishMood {
    HAPPY,      // All stats good
    NEUTRAL,    // Default/okay state
    HUNGRY,     // Hunger is low
    DIRTY,      // Cleanliness is low
    SAD         // Multiple stats low or happiness very low
}

