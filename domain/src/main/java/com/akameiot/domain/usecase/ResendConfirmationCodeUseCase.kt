package com.akameiot.domain.usecase

import com.akameiot.domain.repository.AuthRepository

class ResendConfirmationCodeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resendConfirmationCode(email)
    }
}