package com.akameiot.app.ui.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.di.AppModule

class PairingTokenViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PairingTokenViewModel(
            generatePairingTokenUseCase = AppModule.generatePairingTokenUseCase,
            authSessionManager = AppModule.authSessionManager,
        ) as T
    }
}