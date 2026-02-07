package com.akameiot.app.ui.register

data class RegisterUiState(

    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,

    val isLoading: Boolean = false,
    val error: String? = null
) {

    val passwordsMatch: Boolean
        get() = confirmPassword.isEmpty() || password == confirmPassword

    val isFormValid: Boolean
        get() =
            email.isNotBlank() &&
                    password.isNotBlank() &&
                    passwordsMatch &&
                    acceptedTerms
}

