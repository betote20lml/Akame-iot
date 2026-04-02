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

    suspend fun startResetPassword(email: String)

    suspend fun confirmResetPassword(
        email: String,
        code: String,
        newPassword: String
    )

    suspend fun hasLocalSession(): Boolean

}