package com.akameiot.app.ui.indexfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.di.AppModule

class IndexFactoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IndexFactoryViewModel(
            telemetryDao = AppModule.telemetryDao,
            networkStore = AppModule.networkStore,
            nodeLimitRepository = AppModule.nodeLimitRepository,
        ) as T
}