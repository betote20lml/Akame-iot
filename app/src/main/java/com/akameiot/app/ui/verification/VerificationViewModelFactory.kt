package com.akameiot.app.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmSignUpUseCase
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase


class VerificationViewModelFactory(
    private val confirmSignUpUseCase: ConfirmSignUpUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {

            return VerificationViewModel(
                confirmSignUpUseCase = confirmSignUpUseCase,
                autoLoginUseCase = autoLoginUseCase,
                resendConfirmationCodeUseCase = resendConfirmationCodeUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}