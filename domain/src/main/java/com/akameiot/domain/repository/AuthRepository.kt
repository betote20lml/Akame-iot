package com.akameiot.domain.repository

import com.akameiot.domain.model.RegisterResult

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String
    ): RegisterResult

    suspend fun confirmSignUp(
        email: String,
        code: String
    )

    suspend fun login(
        email: String,
        password: String
    )

    suspend fun isUserLoggedIn(): Boolean

    suspend fun resendConfirmationCode(email: String)
}