package com.charles.virtualpet.fishtank.domain.model

data class FishSkin(
    val id: String,
    val name: String,
    val tintColorHex: String?, // null = classic/untinted look
    val price: Int
)
