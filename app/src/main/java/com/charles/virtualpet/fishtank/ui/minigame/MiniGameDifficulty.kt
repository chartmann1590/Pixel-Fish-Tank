package com.charles.virtualpet.fishtank.ui.minigame

enum class MiniGameDifficulty(val displayName: String, val multiplier: Float) {
    EASY("Easy", 1.0f),      // Baseline speed
    MEDIUM("Medium", 2.0f),  // 200% faster
    HARD("Hard", 4.0f)       // 400% faster
}

