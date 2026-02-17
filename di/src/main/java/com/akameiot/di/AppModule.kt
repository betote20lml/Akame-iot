package com.akameiot.di

import com.akameiot.data.remote.CognitoAuthRemoteDataSource
import com.akameiot.data.repository_impl.AuthRepositoryImpl
import com.akameiot.data.session.CognitoAuthSessionManager
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.RegisterUseCase
import com.akameiot.domain.validation.PasswordValidator
import com.akameiot.domain.usecase.ConfirmSignUpUseCase
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.CheckAuthSessionUseCase
import com.akameiot.domain.usecase.ConfirmResetPasswordUseCase
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase


object AppModule {

    private val cognitoRemote by lazy {
        CognitoAuthRemoteDataSource()
    }

    val authRepository by lazy {
        AuthRepositoryImpl(cognitoRemote)
    }

    val registerUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val authSessionManager: AuthSessionManager by lazy {
        CognitoAuthSessionManager()
    }

    val passwordValidator: PasswordValidator by lazy {
        PasswordValidator()
    }

    val confirmSignUpUseCase by lazy {
        ConfirmSignUpUseCase(authRepository)
    }

    val autoLoginUseCase by lazy {
        AutoLoginUseCase(authRepository)
    }

    val checkAuthSessionUseCase by lazy {
        CheckAuthSessionUseCase(authRepository)
    }

    val resendConfirmationCodeUseCase by lazy {
        ResendConfirmationCodeUseCase(authRepository)
    }

    val startResetPasswordUseCase by lazy {
        StartResetPasswordUseCase(authRepository)
    }

    val confirmResetPasswordUseCase by lazy {
        ConfirmResetPasswordUseCase(authRepository)
    }

}