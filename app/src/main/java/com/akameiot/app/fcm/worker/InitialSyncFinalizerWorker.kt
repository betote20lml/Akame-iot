package com.akameiot.app.fcm.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akameiot.di.AppModule

class InitialSyncFinalizerWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("FinalizerWorker", "Sincronización de todos los dispositivos completada con éxito.")
        AppModule.recoveryStateStore.setInitialSyncFailed(false)
        AppModule.syncInProgress.value = false

        return Result.success()
    }
}