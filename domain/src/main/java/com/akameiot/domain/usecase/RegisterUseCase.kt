package com.akameiot.domain.usecase

import com.akameiot.domain.model.RegisterResult
import com.akameiot.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ): RegisterResult {
        return repository.register(email, password)
    }
}