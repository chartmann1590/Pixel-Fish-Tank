package com.charles.virtualpet.fishtank.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.virtualpet.fishtank.data.FirebaseStoreRepository
import com.charles.virtualpet.fishtank.data.ImageCacheManager

class StoreSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val imageCacheManager = ImageCacheManager(applicationContext)
            val repository = FirebaseStoreRepository(applicationContext, imageCacheManager)
            
            // Sync store items
            repository.syncStoreItems()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

