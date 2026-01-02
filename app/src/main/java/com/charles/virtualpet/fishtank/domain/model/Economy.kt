package com.charles.virtualpet.fishtank.domain.model

data class Economy(
    val coins: Int = 0,
    val inventoryItems: List<InventoryItem> = emptyList()
)

data class InventoryItem(
    val id: String,
    val name: String,
    val type: ItemType,
    val quantity: Int = 1
)

enum class ItemType {
    FOOD,
    DECORATION
}

