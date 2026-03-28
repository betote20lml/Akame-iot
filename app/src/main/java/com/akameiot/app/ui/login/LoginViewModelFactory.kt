package com.akameiot.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.repository.AuthRepository
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase
import com.akameiot.domain.usecase.SyncUserDevicesUseCase
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.data.session.FcmTokenStore

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val startResetPasswordUseCase: StartResetPasswordUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase,
    private val syncUserDevicesUseCase: SyncUserDevicesUseCase,
    private val authSessionManager: AuthSessionManager,
    private val fcmTokenStore: FcmTokenStore
    ) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(
            authRepository,
            startResetPasswordUseCase,
            resendConfirmationCodeUseCase,
            syncUserDevicesUseCase,
            authSessionManager,
            fcmTokenStore
            ) as T
    }
}