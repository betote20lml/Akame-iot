package com.akameiot.domain.usecase

import com.akameiot.domain.repository.AuthRepository

class StartResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        repository.startResetPassword(email)
    }
}