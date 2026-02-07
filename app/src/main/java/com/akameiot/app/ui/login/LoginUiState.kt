package com.akameiot.app.ui.login

data class LoginUiState(

    val email: String = "",
    val password: String = "",

    val isLoading: Boolean = false,
) {

    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
