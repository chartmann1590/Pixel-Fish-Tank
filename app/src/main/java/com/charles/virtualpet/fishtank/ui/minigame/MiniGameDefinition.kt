package com.charles.virtualpet.fishtank.ui.minigame

import androidx.compose.ui.graphics.Color

data class MiniGameDefinition(
    val type: MiniGameType,
    val title: String,
    val description: String,
    val emoji: String,
    val gradientColors: List<Color>
)

