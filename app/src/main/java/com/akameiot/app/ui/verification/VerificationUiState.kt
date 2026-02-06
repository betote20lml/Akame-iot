package com.akameiot.app.ui.verification

data class VerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
