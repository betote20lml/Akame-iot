package com.akameiot.app.ui.qrauth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akameiot.di.AppModule

class QrAuthViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QrAuthViewModel(
            consumeTokenUseCase = AppModule.consumeTokenUseCase,
            authSessionManager = AppModule.authSessionManager,
            app = AppModule.appContext as Application,
        ) as T
    }
}