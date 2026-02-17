package com.akameiot.app.ui.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmResetPasswordUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase
import com.akameiot.domain.validation.PasswordValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val passwordValidator: PasswordValidator,
    private val confirmResetPasswordUseCase: ConfirmResetPasswordUseCase,
    private val startResetPasswordUseCase: StartResetPasswordUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
    private val email: String
) : ViewModel() {

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 60
    }

    private fun startCooldown() {
        viewModelScope.launch {
            for (i in RESEND_COOLDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(resendCooldown = i) }
                kotlinx.coroutines.delay(1000)
            }
            _uiState.update { it.copy(resendCooldown = 0) }
        }
    }

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResetPasswordEvent>(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    private suspend fun sendEvent(event: ResetPasswordEvent) {
        _events.emit(event)
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value) }
    }

    fun resend() {

        val state = _uiState.value
        if (!state.canResend) return

        viewModelScope.launch {

            try {

                startResetPasswordUseCase(email)
                startCooldown()
                sendEvent(ResetPasswordEvent.CodeResent)

            } catch (e: Exception) {

                val message = when {

                    e.message?.contains("LimitExceeded", true) == true ->
                        "Demasiados intentos. Espera un momento."

                    e.message?.contains("TooManyRequests", true) == true ->
                        "Demasiadas solicitudes. Intenta más tarde."

                    else ->
                        "No se pudo reenviar el código"
                }

                sendEvent(ResetPasswordEvent.Error(message))
            }
        }
    }

    fun onPasswordChange(value: String) {

        val validation = passwordValidator.validate(value)

        _uiState.update {
            it.copy(
                password = value,
                passwordValidation = validation
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun submit() {

        val state = _uiState.value
        if (state.isLoading) return

        if (!state.isFormValid) {
            viewModelScope.launch {
                sendEvent(
                    ResetPasswordEvent.Error(
                        "Completa correctamente todos los campos"
                    )
                )
            }
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                confirmResetPasswordUseCase(
                    email = email,
                    code = state.code,
                    newPassword = state.password
                )

                autoLoginUseCase(
                    email = email,
                    password = state.password
                )

                sendEvent(ResetPasswordEvent.Success)

            } catch (e: Exception) {

                val message = when {

                    e.message?.contains("correct", true) == true ||
                            e.message?.contains("mismatch", true) == true -> {

                        _uiState.update { it.copy(code = "") }

                        "El código ingresado es incorrecto"
                    }

                    e.message?.contains("expired", true) == true -> {

                        _uiState.update { it.copy(code = "") }

                        "El código ha expirado. Solicita uno nuevo."
                    }

                    e.message?.contains("exceeded", true) == true ->
                        "Demasiados intentos fallidos. Intenta más tarde."

                    e.message?.contains("invalid", true) == true ->
                        "La contraseña no cumple los requisitos"

                    else ->
                        "No se pudo cambiar la contraseña"
                }

                sendEvent(ResetPasswordEvent.Error(message))
            } finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

}