package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.data.remote.RemoteConfigService
import com.akameiot.domain.usecase.CheckLocalSessionUseCase

class SplashViewModelFactory(
    private val checkLocalSessionUseCase: CheckLocalSessionUseCase,
    private val remoteConfigService: RemoteConfigService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return SplashViewModel(
            checkLocalSessionUseCase,
            remoteConfigService
        ) as T
    }
}