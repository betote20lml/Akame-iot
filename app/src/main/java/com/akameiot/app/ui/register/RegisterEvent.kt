package com.akameiot.app.ui.register

sealed class RegisterEvent {

    data class Success(
        val requiresConfirmation: Boolean
    ) : RegisterEvent()

    data class Error(val message: String) : RegisterEvent()
}