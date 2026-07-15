package com.charles.virtualpet.fishtank.data

import com.charles.virtualpet.fishtank.domain.model.FishSkin

object FishSkinCatalog {
    val availableSkins = listOf(
        FishSkin(
            id = "classic",
            name = "Classic",
            tintColorHex = null,
            price = 0
        ),
        FishSkin(
            id = "ocean_blue",
            name = "Ocean Blue",
            tintColorHex = "#5DADE2",
            price = 75
        ),
        FishSkin(
            id = "coral_pink",
            name = "Coral Pink",
            tintColorHex = "#FF8FA3",
            price = 125
        ),
        FishSkin(
            id = "mint_green",
            name = "Mint Green",
            tintColorHex = "#7DDDB0",
            price = 175
        ),
        FishSkin(
            id = "royal_gold",
            name = "Royal Gold",
            tintColorHex = "#FFD54A",
            price = 250
        )
    )

    fun getSkinById(id: String): FishSkin? = availableSkins.find { it.id == id }
}
