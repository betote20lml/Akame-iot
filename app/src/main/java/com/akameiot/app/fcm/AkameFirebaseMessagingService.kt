package com.akameiot.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.akameiot.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AkameFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val meshid = message.data["meshid"] ?: "desconocido"
        val ts = message.data["ts"] ?: ""
        Log.d("FCM", "Mensaje recibido: ${message.data}")
        FcmEventBus.send("Nuevo dato de $meshid ts=$ts")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "Nuevo token FCM: $token")

        CoroutineScope(Dispatchers.IO).launch {

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