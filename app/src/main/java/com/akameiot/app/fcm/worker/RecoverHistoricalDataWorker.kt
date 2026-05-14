package com.akameiot.app.fcm.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akameiot.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecoverHistoricalDataWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val meshId = inputData.getString("meshId") ?: return@withContext Result.failure()
        val fromTs = inputData.getLong("fromTs", 0L)
        val toTs   = inputData.getLong("toTs", 0L)

        if (fromTs == 0L || toTs == 0L) return@withContext Result.failure()

        AppModule.syncInProgress.value = true
        Log.d("RecoverWorker", "meshId=$meshId iniciando")

        return@withContext try {
            AppModule.syncRecentTelemetryUseCase.recoverWindow(
                meshId = meshId,
                fromTs = fromTs,
                toTs   = toTs,
            )

            Log.d(
                "RecoverWorker",
                "meshId=$meshId recovery completada."
            )

            Result.success()

        } catch (e: Exception) {
            Log.e("RecoverWorker", "meshId=$meshId error: ${e.message}")
            Result.failure()
        }
    }
}