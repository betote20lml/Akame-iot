package com.akameiot.app.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmSignUpUseCase


class VerificationViewModelFactory(
    private val confirmSignUpUseCase: ConfirmSignUpUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {

            return VerificationViewModel(
                confirmSignUpUseCase = confirmSignUpUseCase,
                autoLoginUseCase = autoLoginUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}