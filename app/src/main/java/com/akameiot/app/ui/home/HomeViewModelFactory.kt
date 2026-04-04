package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.di.AppModule

class HomeViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            activateDeviceUseCase = AppModule.activateDeviceUseCase,
            authSessionManager = AppModule.authSessionManager,
            getAppUserUseCase = AppModule.getAppUserUseCase,
            networkManager = AppModule.networkManager,
            tokenStore = AppModule.tokenStore,
            telemetryDao = AppModule.telemetryDao,
            networkStore = AppModule.networkStore,
            filterPreferencesStore = AppModule.filterPreferencesStore,
            calculateMeshWindowUseCase = AppModule.calculateMeshWindowUseCase
        ) as T
    }
}