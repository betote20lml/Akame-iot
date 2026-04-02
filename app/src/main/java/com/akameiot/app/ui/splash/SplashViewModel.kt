package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.akameiot.domain.usecase.CheckLocalSessionUseCase


class SplashViewModel(
    private val checkLocalSessionUseCase: CheckLocalSessionUseCase
) : ViewModel() {

    fun checkSession(
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val isLoggedIn = checkLocalSessionUseCase()
            onResult(isLoggedIn)
        }
    }
}