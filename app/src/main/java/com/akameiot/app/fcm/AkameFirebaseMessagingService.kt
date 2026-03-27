package com.akameiot.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.akameiot.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

        serviceScope.launch {
            try {
                // El use case decide internamente si hay que hacer fetch
                AppModule.syncRecentTelemetryUseCase(meshid, notifTs)
                Log.d("FCM", "Sync completado para $meshid")
                // Emitir al bus DESPUÉS de persistir, no antes
                FcmEventBus.send("Nuevo dato de $meshid ts=$notifTs")
            } catch (e: Exception) {
                Log.e("FCM", "Error en sync de telemetría", e)
            }
        }
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