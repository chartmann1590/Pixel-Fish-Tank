package com.charles.virtualpet.fishtank.domain.model

data class Decoration(
    val id: String,
    val name: String,
    val drawableRes: String, // Resource name like "decoration_plant"
    val price: Int,
    val type: DecorationType
)

enum class DecorationType {
    PLANT,
    ROCK,
    TOY
}

data class PlacedDecoration(
    val decorationId: String,
    val x: Float, // Position as percentage (0.0 to 1.0)
    val y: Float  // Position as percentage (0.0 to 1.0)
)

