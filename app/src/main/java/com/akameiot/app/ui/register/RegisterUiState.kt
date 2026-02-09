package com.akameiot.app.ui.register

import com.akameiot.core.utils.isValidEmail
data class RegisterUiState(

    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,

    val isLoading: Boolean = false,
) {

    val passwordsMatch: Boolean
        get() = confirmPassword.isNotBlank() && password == confirmPassword

    val isFormValid: Boolean
        get() =
               email.isValidEmail() &&
                    password.isNotBlank() &&
                    passwordsMatch &&
                    acceptedTerms
}

