package com.akameiot.app.ui.register

import com.akameiot.core.utils.isValidEmail
import com.akameiot.domain.validation.PasswordValidationResult

data class RegisterUiState(

    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,

    val passwordValidation: PasswordValidationResult =
        PasswordValidationResult(
            hasMinLength = false,
            hasNumber = false,
            hasLowercase = false
        ),

    val isLoading: Boolean = false,
) {

    val passwordsMatch: Boolean
        get() = confirmPassword.isNotBlank() && password == confirmPassword

    val isFormValid: Boolean
        get() =
            email.isValidEmail() &&
                    passwordValidation.isValid &&
                    passwordsMatch &&
                    acceptedTerms
}
