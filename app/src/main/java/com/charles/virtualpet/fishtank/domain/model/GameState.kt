package com.charles.virtualpet.fishtank.domain.model

data class GameState(
    val fishState: FishState = FishState(),
    val economy: Economy = Economy(),
    val tankLayout: TankLayout = TankLayout(),
    val dailyTasks: DailyTasksState = DailyTasksState(),
    val settings: Settings = Settings()
)

data class TankLayout(
    val placedDecorations: List<PlacedDecoration> = emptyList()
)
