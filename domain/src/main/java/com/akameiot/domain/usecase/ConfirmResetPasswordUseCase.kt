package com.akameiot.domain.usecase

import com.akameiot.domain.repository.AuthRepository

class ConfirmResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        code: String,
        newPassword: String
    ) {
        repository.confirmResetPassword(
            email,
            code,
            newPassword
        )
    }
}