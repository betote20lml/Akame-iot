package com.akameiot.app.ui.verification

sealed interface VerificationEvent {

    object Success : VerificationEvent
    object CodeResent : VerificationEvent

    data class Error(
        val message: String
    ) : VerificationEvent
}