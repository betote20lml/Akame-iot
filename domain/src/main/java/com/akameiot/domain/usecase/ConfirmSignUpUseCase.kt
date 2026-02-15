package com.akameiot.domain.usecase

import com.akameiot.domain.repository.AuthRepository

class ConfirmSignUpUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        code: String
    ) {
        authRepository.confirmSignUp(email, code)
    }
}