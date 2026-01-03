package com.charles.virtualpet.fishtank.data

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String, val throwable: Throwable?) : SyncState()
}

