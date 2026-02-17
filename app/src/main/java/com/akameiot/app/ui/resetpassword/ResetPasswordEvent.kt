package com.akameiot.app.ui.resetpassword

sealed interface ResetPasswordEvent {

    object Success : ResetPasswordEvent
    object CodeResent : ResetPasswordEvent

    data class Error(
        val message: String
    ) : ResetPasswordEvent
}