package com.akameiot.app.fcm.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akameiot.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncTelemetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val meshId = inputData.getString("meshId") ?: return@withContext Result.failure()
        val notifTs = inputData.getLong("notifTs", 0L)

        android.util.Log.d("SyncWorker", "Starting sync meshId=$meshId notifTs=$notifTs")

        return@withContext try {
            AppModule.syncRecentTelemetryUseCase(meshId, notifTs)
            android.util.Log.d("SyncWorker", "Sync success meshId=$meshId")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Sync failed meshId=$meshId", e)
            Result.retry()
        }
    }
}