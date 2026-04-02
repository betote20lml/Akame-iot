package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.domain.usecase.CheckLocalSessionUseCase

class SplashViewModelFactory(
    private val checkLocalSessionUseCase: CheckLocalSessionUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(checkLocalSessionUseCase) as T
    }
}