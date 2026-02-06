package com.akameiot.app.ui.register

data class RegisterUiState(

    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,

    val isLoading: Boolean = false,
    val error: String? = null
)
