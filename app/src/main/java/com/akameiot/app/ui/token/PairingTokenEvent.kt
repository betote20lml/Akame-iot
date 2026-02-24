package com.akameiot.app.ui.token

sealed interface PairingTokenEvent {
    data class ShowError(val message: String) : PairingTokenEvent
    object NavigateToLogin : PairingTokenEvent
}