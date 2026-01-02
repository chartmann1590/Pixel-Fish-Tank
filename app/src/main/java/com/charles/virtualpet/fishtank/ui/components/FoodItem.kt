package com.charles.virtualpet.fishtank.ui.components

data class FoodItem(
    val id: String,
    val x: Float, // Position as fraction of container width (0.0 to 1.0)
    val y: Float, // Position as fraction of container height (0.0 to 1.0)
    val startTime: Long = System.currentTimeMillis(),
    val hasReachedBottom: Boolean = false // Track if food reached bottom without being eaten
)

