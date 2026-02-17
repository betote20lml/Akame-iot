package com.akameiot.data.repository_impl

import com.akameiot.data.remote.CognitoAuthRemoteDataSource
import com.akameiot.domain.model.RegisterResult
import com.akameiot.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: CognitoAuthRemoteDataSource
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String
    ): RegisterResult {
        return remote.register(email, password)
    }

    override suspend fun confirmSignUp(
        email: String,
        code: String
    ) {
        remote.confirmSignUp(email, code)
    }

    override suspend fun login(
        email: String,
        password: String
    ) {
        remote.login(email, password)
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return remote.isUserLoggedIn()
    }

    override suspend fun resendConfirmationCode(email: String) {
        remote.resendConfirmationCode(email)
    }

    override suspend fun startResetPassword(email: String) {
        remote.startResetPassword(email)
    }

    override suspend fun confirmResetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        remote.confirmResetPassword(
            email,
            code,
            newPassword
        )
    }

}
