package com.akameiot.app.ui.login

import com.akameiot.core.utils.isValidEmail
data class LoginUiState(

    val email: String = "",
    val password: String = "",

    val isLoading: Boolean = false,
) {

    val isFormValid: Boolean
        get() = email.isValidEmail() && password.isNotBlank()
}
