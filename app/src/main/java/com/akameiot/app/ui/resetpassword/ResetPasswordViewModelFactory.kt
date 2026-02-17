package com.akameiot.app.ui.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmResetPasswordUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase
import com.akameiot.domain.validation.PasswordValidator

class ResetPasswordViewModelFactory(
    private val passwordValidator: PasswordValidator,
    private val confirmResetPasswordUseCase: ConfirmResetPasswordUseCase,
    private val startResetPasswordUseCase: StartResetPasswordUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
    private val email: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return ResetPasswordViewModel(
                passwordValidator,
                confirmResetPasswordUseCase,
                startResetPasswordUseCase,
                autoLoginUseCase,
                email
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
