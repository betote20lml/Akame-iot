package com.akameiot.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.RegisterUseCase
import com.akameiot.domain.validation.PasswordValidator

class RegisterViewModelFactory(
    private val registerUseCase: RegisterUseCase,
    private val passwordValidator: PasswordValidator
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(
                registerUseCase,
                passwordValidator
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
