package com.akameiot.app.ui.resetpassword

import com.akameiot.domain.validation.PasswordValidationResult

data class ResetPasswordUiState(

    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val passwordValidation: PasswordValidationResult =
        PasswordValidationResult(
            hasMinLength = false,
            hasNumber = false,
            hasLowercase = false
        ),

    val isLoading: Boolean = false
) {

    val passwordsMatch: Boolean
        get() = confirmPassword.isNotBlank() && password == confirmPassword

    val isFormValid: Boolean
        get() =
            code.length == 6 &&
                    passwordValidation.isValid &&
                    passwordsMatch
}