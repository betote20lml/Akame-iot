package com.akameiot.domain.usecase


import com.akameiot.domain.repository.AuthRepository

class AutoLoginUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) {
        authRepository.login(email, password)
    }
}