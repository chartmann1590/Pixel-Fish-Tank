package com.charles.virtualpet.fishtank.data

import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.model.Decoration
import com.charles.virtualpet.fishtank.domain.model.DecorationType

object DecorationStore {
    val availableDecorations = listOf(
        Decoration(
            id = "plant_1",
            name = "Aquatic Plant",
            drawableRes = "decoration_plant",
            price = 50,
            type = DecorationType.PLANT
        ),
        Decoration(
            id = "rock_1",
            name = "Decorative Rock",
            drawableRes = "decoration_rock",
            price = 30,
            type = DecorationType.ROCK
        ),
        Decoration(
            id = "toy_1",
            name = "Fish Toy",
            drawableRes = "decoration_toy",
            price = 75,
            type = DecorationType.TOY
        )
    )

    fun getDecorationById(id: String): Decoration? {
        return availableDecorations.find { it.id == id }
    }
}

