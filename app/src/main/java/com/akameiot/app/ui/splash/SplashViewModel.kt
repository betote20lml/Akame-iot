package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.akameiot.domain.usecase.CheckAuthSessionUseCase

class SplashViewModel(
    private val checkAuthSessionUseCase: CheckAuthSessionUseCase
) : ViewModel() {

    fun checkSession(
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val isLoggedIn = checkAuthSessionUseCase()
            onResult(isLoggedIn)
        }
    }
}