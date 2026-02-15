package com.akameiot.domain.validation

class PasswordValidator {

    fun validate(password: String): PasswordValidationResult {
        return PasswordValidationResult(
            hasMinLength = password.length >= 8,
            hasNumber = password.any { it.isDigit() },
            hasLowercase = password.any { it.isLowerCase() },
        )
    }
}