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

        return@withContext try {

            AppModule.syncRecentTelemetryUseCase(meshId, notifTs)

            Result.success()

        } catch (e: Exception) {

            Result.retry()
        }
    }
}