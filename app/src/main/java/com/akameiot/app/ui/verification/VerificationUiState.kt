package com.akameiot.app.ui.verification

data class VerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val resendCooldown: Int = 0,
) {

    val isCodeValid: Boolean
        get() = code.length == 6

    val canResend: Boolean
        get() = resendCooldown == 0 && !isLoading
}