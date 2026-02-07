package com.akameiot.app.ui.qrauth

sealed interface QrAuthEvent {

    object Success : QrAuthEvent

    data class Error(
        val message: String
    ) : QrAuthEvent
}