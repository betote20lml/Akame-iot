package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.CheckAuthSessionUseCase

class SplashViewModelFactory(
    private val checkAuthSessionUseCase: CheckAuthSessionUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(checkAuthSessionUseCase) as T
    }
}