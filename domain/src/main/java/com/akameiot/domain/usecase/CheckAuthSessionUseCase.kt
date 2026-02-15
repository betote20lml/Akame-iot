package com.akameiot.domain.usecase

import com.akameiot.domain.repository.AuthRepository

class CheckAuthSessionUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}