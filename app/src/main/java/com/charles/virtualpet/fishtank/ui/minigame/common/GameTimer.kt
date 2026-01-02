package com.charles.virtualpet.fishtank.ui.minigame.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun useGameTimer(
    durationSeconds: Int = 30,
    isActive: Boolean
): Pair<Int, Boolean> {
    var timeRemaining by remember { mutableIntStateOf(durationSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            isRunning = true
            timeRemaining = durationSeconds
            while (timeRemaining > 0 && isActive) {
                delay(1000)
                timeRemaining--
            }
            isRunning = false
        } else {
            isRunning = false
            timeRemaining = durationSeconds
        }
    }
    
    return Pair(timeRemaining, isRunning)
}

