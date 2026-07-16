package com.charles.virtualpet.fishtank.playgames

import com.charles.virtualpet.fishtank.domain.model.DailyTask
import com.charles.virtualpet.fishtank.domain.model.DailyTasksState
import com.charles.virtualpet.fishtank.domain.model.Economy
import com.charles.virtualpet.fishtank.domain.model.FishState
import com.charles.virtualpet.fishtank.domain.model.GameState
import com.charles.virtualpet.fishtank.domain.model.InventoryItem
import com.charles.virtualpet.fishtank.domain.model.ItemType
import com.charles.virtualpet.fishtank.domain.model.PlacedDecoration
import com.charles.virtualpet.fishtank.domain.model.TankLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayGamesMilestonesTest {
    @Test
    fun `returns every earned milestone for an established player`() {
        val state = GameState(
            fishState = FishState(level = 10),
            economy = Economy(
                inventoryItems = (1..5).map { index ->
                    InventoryItem("item-$index", "Item $index", ItemType.DECORATION)
                }
            ),
            tankLayout = TankLayout(
                placedDecorations = (1..5).map { index ->
                    PlacedDecoration("placed-$index", "item-$index", 0.5f, 0.5f)
                }
            ),
            dailyTasks = DailyTasksState(
                tasks = listOf(
                    DailyTask("feed_fish", "Feed", "", 0, 0, true),
                    DailyTask("clean_tank", "Clean", "", 0, 0, true)
                ),
                longestStreak = 7
            )
        )

        assertEquals(PlayGamesAchievement.entries.toSet(), PlayGamesMilestones.achievementsFor(state))
    }

    @Test
    fun `does not award milestones for a new player`() {
        assertEquals(emptySet<PlayGamesAchievement>(), PlayGamesMilestones.achievementsFor(GameState()))
    }
}
