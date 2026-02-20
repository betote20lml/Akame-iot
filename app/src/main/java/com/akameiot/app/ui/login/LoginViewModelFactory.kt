package com.akameiot.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.repository.AuthRepository
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val startResetPasswordUseCase: StartResetPasswordUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(
            authRepository,
            startResetPasswordUseCase,
            resendConfirmationCodeUseCase
            ) as T
    }
}