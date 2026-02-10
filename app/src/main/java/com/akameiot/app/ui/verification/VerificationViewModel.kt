package com.akameiot.app.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerificationViewModel : ViewModel() {

    // STATE (igual que Register)
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

                //  simulación Cognito
                delay(1500)

                sendEvent(VerificationEvent.Success)

            } catch (e: Exception) {

                sendEvent(
                    VerificationEvent.Error("Código inválido")
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

                // llamar cognito resend

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


