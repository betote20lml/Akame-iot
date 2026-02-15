package com.akameiot.app.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.akameiot.app.session.AuthTempStorage
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.ConfirmSignUpUseCase


class VerificationViewModel (
    private val confirmSignUpUseCase: ConfirmSignUpUseCase,
    private val autoLoginUseCase: AutoLoginUseCase
    ): ViewModel() {


    private val email = AuthTempStorage.email.orEmpty()
    private val password = AuthTempStorage.password.orEmpty()

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState = _uiState.asStateFlow()

    // EVENTS
    private val _events = MutableSharedFlow<VerificationEvent>(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()


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
                AuthTempStorage.clear()
                sendEvent(VerificationEvent.Success)

            } catch (e: Exception) {

                sendEvent(
                    VerificationEvent.Error(
                        e.message ?: "Código inválido"
                    )
                )

            } finally {

                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }


    fun resend() {

        viewModelScope.launch {

            try {

                sendEvent(
                    VerificationEvent.Error("Código reenviado")
                )

            } catch (e: Exception) {

                sendEvent(
                    VerificationEvent.Error("No se pudo reenviar")
                )
            }
        }
    }
}


