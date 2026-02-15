package com.akameiot.domain.validation

data class PasswordValidationResult(
    val hasMinLength: Boolean,
    val hasNumber: Boolean,
    val hasLowercase: Boolean,
) {
    val isValid: Boolean
        get() = hasMinLength && hasNumber && hasLowercase
}