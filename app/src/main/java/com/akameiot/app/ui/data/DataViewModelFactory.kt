package com.akameiot.app.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.di.AppModule

class DataViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DataViewModel(
            telemetryDao = AppModule.telemetryDao,
            networkStore = AppModule.networkStore,
            getAppUserUseCase = AppModule.getAppUserUseCase,
        ) as T
}