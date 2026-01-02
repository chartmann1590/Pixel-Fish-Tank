package com.charles.virtualpet.fishtank.domain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.charles.virtualpet.fishtank.data.GameStateRepository

class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            val repository = GameStateRepository(context)
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(context.applicationContext as android.app.Application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

