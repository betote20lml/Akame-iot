package com.akameiot.app.ui.register

sealed class RegisterEvent {

    object Success : RegisterEvent()

    data class Error(val message: String) : RegisterEvent()
}