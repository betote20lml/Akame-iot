package com.akameiot.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

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
    }
}