package com.akameiot.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.akameiot.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AkameFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val meshid = message.data["meshid"] ?: return
        val tsStr = message.data["ts"] ?: ""
        val rawTs = tsStr.toLongOrNull() ?: 0L
        val notifTs = if (rawTs > 1_000_000_000_000) rawTs / 1000 else rawTs

        Log.d("FCM", "Mensaje recibido: meshid=$meshid ts=$notifTs")

        val nowSeconds = System.currentTimeMillis() / 1000L
        AppModule.lastSeenPerMesh.update { current ->
            current + (meshid to nowSeconds)
        }
        AppModule.freshnessWakeUp.trySend(Unit)
        enqueueSync(meshid, notifTs)
    }

    private fun enqueueSync(meshId: String, notifTs: Long) {

        val work = OneTimeWorkRequestBuilder<com.akameiot.app.fcm.worker.SyncTelemetryWorker>()
            .setInputData(
                workDataOf(
                    "meshId" to meshId,
                    "notifTs" to notifTs
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "sync_$meshId",
                ExistingWorkPolicy.REPLACE,
                work
            )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "Nuevo token FCM: $token")

        serviceScope.launch {

            try {

                val tokenStore = AppModule.tokenStore

                val oldToken = tokenStore.getToken()

                if (oldToken == token) {
                    Log.d("FCM", "Token unchanged, skipping")
                    return@launch
                }

                tokenStore.saveToken(token)
                tokenStore.markNeedsResubscribe()

                val authToken = AppModule.authSessionManager.fetchIdToken()

                AppModule.networkManager.resubscribeAll(authToken)

                tokenStore.clearResubscribeFlag()

            } catch (e: Exception) {

                Log.e("FCM", "Resubscribe postponed", e)

            }
        }
    }
}