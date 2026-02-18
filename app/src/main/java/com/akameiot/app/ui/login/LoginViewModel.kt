package com.akameiot.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.akameiot.core.utils.isValidEmail
import com.akameiot.domain.repository.AuthRepository
import com.akameiot.domain.usecase.StartResetPasswordUseCase
import com.amplifyframework.auth.cognito.exceptions.service.UserNotConfirmedException

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val startResetPasswordUseCase: StartResetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<LoginEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    private fun sendEvent(event: LoginEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun onForgotPasswordClick() {

        val email = _uiState.value.email

        if (!email.isValidEmail()) {
            sendEvent(LoginEvent.Error("Ingresa un correo válido"))
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                startResetPasswordUseCase(email)

                sendEvent(LoginEvent.NavigateToPasswordRecovery)

            } catch (e: Exception) {

                sendEvent(
                    LoginEvent.Error(
                        e.message ?: "No se pudo iniciar la recuperación"
                    )
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun login() {

        val state = _uiState.value

        if (state.isLoading) return

        if (!state.isFormValid) {
            sendEvent(LoginEvent.Error("Completa correo y contraseña"))
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                authRepository.login(
                    email = state.email,
                    password = state.password
                )

                sendEvent(LoginEvent.Success)

            } catch (e: Exception) {

                when {

                    e is UserNotConfirmedException -> {

                        sendEvent(
                            LoginEvent.NavigateToVerification(state.email)
                        )
                    }

                    e.message?.contains("Failed", true) == true ||
                            e.message?.contains("Incorrect username or password", true) == true -> {

                        sendEvent(LoginEvent.Error("Correo o contraseña incorrectos"))
                    }

                    e.message?.contains("UserNotFound", true) == true -> {

                        sendEvent(LoginEvent.Error("El usuario no existe"))
                    }

                    else -> {

                        sendEvent(LoginEvent.Error("No se pudo iniciar sesión"))
                    }
                }
            }
             finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

