package com.charles.virtualpet.fishtank.ui.tutorial

data class TutorialSlide(
    val title: String,
    val description: String,
    val iconRes: Int? = null, // Drawable resource ID
    val emoji: String? = null, // Fallback emoji
    val backgroundColor: androidx.compose.ui.graphics.Color? = null
)

object TutorialSlides {
    val slides = listOf(
        TutorialSlide(
            title = "Welcome to Pixel Fish Tank!",
            description = "Your fish needs your care! Watch the stats at the top: Hunger, Cleanliness, and Happiness. Keep them high for a happy fish. Tap the menu button (☰) in the bottom right to access all actions.",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.fish_starter,
            emoji = "🐟"
        ),
        TutorialSlide(
            title = "Feed Your Fish",
            description = "Tap the Feed button to drop food into the tank. Your fish will swim to eat it! This increases Hunger and gives a small Happiness boost. Complete daily feed tasks to earn coins and XP.",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.ic_feed,
            emoji = "🍽️"
        ),
        TutorialSlide(
            title = "Clean the Tank",
            description = "Tap the Clean button to refresh the tank water. This resets Cleanliness to 100% and boosts Happiness. A dirty tank makes your fish sad, so clean regularly! You'll earn rewards for daily cleaning tasks.",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.ic_clean,
            emoji = "🧹"
        ),
        TutorialSlide(
            title = "Play Mini-Games",
            description = "Tap Mini-Game to play fun games and earn coins and XP! Complete daily mini-game tasks to boost your rewards. Check the Daily Tasks card to see what you need to do. The more you play, the more you earn!",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.coin,
            emoji = "🎮"
        ),
        TutorialSlide(
            title = "Decorate Your Tank",
            description = "Use coins to buy decorations in the Store. Then tap Decorate to place them in your tank! Decorations boost your fish's Happiness. Tap placed decorations to remove them. Make your tank beautiful!",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.decoration_plant,
            emoji = "🎨"
        ),
        TutorialSlide(
            title = "Grow and Level Up",
            description = "Complete daily tasks and play mini-games to earn XP. Every 100 XP = 1 level up! Your fish grows bigger as it levels. Watch the Level, XP, and Coins at the bottom of the screen. Check the mood indicator to see how your fish feels!",
            iconRes = com.charles.virtualpet.fishtank.R.drawable.fish_happy,
            emoji = "⭐"
        )
    )
}

