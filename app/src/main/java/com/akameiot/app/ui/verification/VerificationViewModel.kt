package com.akameiot.app.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmSignUpUseCase
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase


class VerificationViewModel (
    private val email: String,
    private val password: String,
    private val confirmSignUpUseCase: ConfirmSignUpUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase
    ): ViewModel() {

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 60
    }

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState = _uiState.asStateFlow()

    // EVENTS
    private val _events = MutableSharedFlow<VerificationEvent>(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    private fun startCooldown() {

        viewModelScope.launch {

            for (i in RESEND_COOLDOWN_SECONDS downTo 1) {

                _uiState.update {
                    it.copy(resendCooldown = i)
                }

                kotlinx.coroutines.delay(1000)
            }

            _uiState.update {
                it.copy(resendCooldown = 0)
            }
        }
    }
    fun onCodeChange(code: String) {
        _uiState.update {
            it.copy(
                code = code
            )
        }
    }

    private suspend fun sendEvent(event: VerificationEvent) {
        _events.emit(event)
    }



    fun verify() {

        val state = _uiState.value
        if (state.isLoading) return

        if (!state.isCodeValid) {

            viewModelScope.launch {
                sendEvent(VerificationEvent.Error("Código incompleto"))
            }

            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Confirmar Registro y autologin
                confirmSignUpUseCase(
                    email = email,
                    code = state.code
                )
                 autoLoginUseCase(
                    email = email,
                    password = password
                )
                sendEvent(VerificationEvent.Success)

            } catch (e: Exception) {

                val message = when {

                    e.message?.contains("correct", true) == true ||
                            e.message?.contains("not correct", true) == true -> {

                        _uiState.update { it.copy(code = "") }

                        "El código ingresado es incorrecto"
                    }

                    e.message?.contains("expired", true) == true ->
                        "El código ha expirado. Solicita uno nuevo."

                    e.message?.contains("exceeded", true) == true ->
                        "Demasiados intentos fallidos. Intenta más tarde."

                    else ->
                        "No se pudo verificar el código"
                }

                sendEvent(VerificationEvent.Error(message))

            } finally {

                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }


    fun resend() {

        val state = _uiState.value
        if (!state.canResend) return

        viewModelScope.launch {

            try {

                resendConfirmationCodeUseCase(email)

                startCooldown()
                sendEvent(VerificationEvent.CodeResent)

            } catch (e: Exception) {

                val message = when {

                    e.message?.contains("LimitExceeded", true) == true ->
                        "Demasiados intentos. Espera un momento."

                    e.message?.contains("TooManyRequests", true) == true ->
                        "Demasiadas solicitudes. Intenta más tarde."

                    else ->
                        "No se pudo reenviar el código"
                }

                sendEvent(VerificationEvent.Error(message))
            }
        }
    }

}


