package com.akameiot.app.ui.verification

sealed interface VerificationEvent {

    object Success : VerificationEvent

    data class Error(
        val message: String
    ) : VerificationEvent
}