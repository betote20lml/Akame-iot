package com.akameiot.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.akameiot.core.utils.isValidEmail

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()


    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onForgotPasswordClick() {

        val email = _uiState.value.email

        if (!email.isValidEmail()) {

            viewModelScope.launch {
                _events.emit(
                    LoginEvent.Error("Ingresa tu correo primero")
                )
            }

            return
        }

        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToPasswordRecovery)
        }
    }

    fun login() {

        val state = _uiState.value

        if (!state.isFormValid) {

            viewModelScope.launch {
                _events.emit(
                    LoginEvent.Error("Completa correo y contraseña")
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                //  Simulación Cognito
                delay(1500)

                _events.emit(LoginEvent.Success)

            } catch (e: Exception) {

                _events.emit(
                    LoginEvent.Error("No se pudo iniciar sesión")
                )

            } finally {

                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}

