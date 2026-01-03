package com.charles.virtualpet.fishtank.domain.model

data class Decoration(
    val id: String,
    val name: String,
    val drawableRes: String, // Resource name like "decoration_plant"
    val imageUrl: String? = null, // Firebase Storage URL
    val price: Int,
    val type: DecorationType
)

enum class DecorationType {
    PLANT,
    ROCK,
    TOY
}

data class PlacedDecoration(
    val id: String, // Unique ID for this placed decoration instance
    val decorationId: String, // The decoration type ID
    val x: Float, // Position as percentage (0.0 to 1.0)
    val y: Float  // Position as percentage (0.0 to 1.0)
)

