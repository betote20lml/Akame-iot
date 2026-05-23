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

        val meshIds = inputData.getStringArray("meshIds")
            ?.toList()
            ?: return@withContext Result.failure()

        val fromTs = inputData.getLong("fromTs", 0L)
        val toTs = inputData.getLong("toTs", 0L)

        if (fromTs == 0L || toTs == 0L) {
            return@withContext Result.failure()
        }

        return@withContext try {

            AppModule.syncInProgress.value = true

            Log.d(
                "RecoverWorker",
                "meshIds=$meshIds iniciando"
            )

            AppModule.syncRecentTelemetryUseCase.recoverWindow(
                meshIds = meshIds,
                fromTs = fromTs,
                toTs = toTs,
            )

            Log.d(
                "RecoverWorker",
                "meshIds=$meshIds recovery completada."
            )

            AppModule.syncInProgress.value = false

            Result.success()

        } catch (e: java.net.SocketTimeoutException) {

            Log.e(
                "RecoverWorker",
                "Socket timeout recovery meshIds=$meshIds",
                e
            )

            AppModule.recoveryStateStore.setInitialSyncFailed(true)
            AppModule.syncInProgress.value = false

            Result.failure()

        } catch (e: java.io.IOException) {

            Log.e(
                "RecoverWorker",
                "Network IO recovery meshIds=$meshIds error=${e.message}",
                e
            )

            AppModule.recoveryStateStore.setInitialSyncFailed(true)
            AppModule.syncInProgress.value = false

            Result.failure()

        } catch (e: Exception) {

            Log.e(
                "RecoverWorker",
                "meshIds=$meshIds fatal error=${e.message}",
                e
            )

            AppModule.recoveryStateStore.setInitialSyncFailed(true)
            AppModule.syncInProgress.value = false

            Result.failure()
        }
    }
}