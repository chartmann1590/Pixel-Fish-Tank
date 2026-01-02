package com.charles.virtualpet.fishtank.ui.minigame

import androidx.compose.ui.graphics.Color

object MiniGameRegistry {
    val allGames: List<MiniGameDefinition> = listOf(
        MiniGameDefinition(
            type = MiniGameType.BUBBLE_POP,
            title = "🫧 Bubble Pop",
            description = "Tap bubbles as they float up!",
            emoji = "🫧",
            gradientColors = listOf(
                Color(0xFF64B5F6),
                Color(0xFF42A5F5),
                Color(0xFF1E88E5)
            )
        ),
        MiniGameDefinition(
            type = MiniGameType.TIMING_BAR,
            title = "⏱️ Timing Bar",
            description = "Stop the marker at the perfect moment!",
            emoji = "⏱️",
            gradientColors = listOf(
                Color(0xFFFFB74D),
                Color(0xFFFFA726),
                Color(0xFFFF9800)
            )
        ),
        MiniGameDefinition(
            type = MiniGameType.CLEANUP_RUSH,
            title = "🧹 Cleanup Rush",
            description = "Tap algae spots to clean the tank!",
            emoji = "🧹",
            gradientColors = listOf(
                Color(0xFF66BB6A),
                Color(0xFF4CAF50),
                Color(0xFF388E3C)
            )
        ),
        MiniGameDefinition(
            type = MiniGameType.FOOD_DROP,
            title = "🍽️ Food Drop",
            description = "Drop food and guide your fish to catch it!",
            emoji = "🍽️",
            gradientColors = listOf(
                Color(0xFFFF6B6B),
                Color(0xFFFF5252),
                Color(0xFFE53935)
            )
        ),
        MiniGameDefinition(
            type = MiniGameType.MEMORY_SHELLS,
            title = "🐚 Memory Shells",
            description = "Remember which shell hides the star!",
            emoji = "🐚",
            gradientColors = listOf(
                Color(0xFFFFD54F),
                Color(0xFFFFCA28),
                Color(0xFFFFC107)
            )
        ),
        MiniGameDefinition(
            type = MiniGameType.FISH_FOLLOW,
            title = "🐟 Fish Follow",
            description = "Repeat the sequence of directions!",
            emoji = "🐟",
            gradientColors = listOf(
                Color(0xFF81C784),
                Color(0xFF66BB6A),
                Color(0xFF4CAF50)
            )
        )
    )
    
    fun getDefinition(type: MiniGameType): MiniGameDefinition? {
        return allGames.find { it.type == type }
    }
}

