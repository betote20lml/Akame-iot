package com.akameiot.app.ui.verification

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VerificationViewModel : ViewModel() {

    var uiState by mutableStateOf(VerificationUiState())
        private set


    private val _events = Channel<VerificationEvent>()
    val events = _events.receiveAsFlow()


    fun onCodeChange(code: String) {
        uiState = uiState.copy(
            code = code,
            error = null
        )
    }


    fun verify() {

        if (uiState.code.length < 6) {
            uiState = uiState.copy(
                error = "Código incompleto"
            )
            return
        }

        viewModelScope.launch {

            uiState = uiState.copy(isLoading = true)

            try {

                // SIMULACIÓN —  Cognito
                kotlinx.coroutines.delay(1500)

                _events.send(VerificationEvent.Success)

            } catch (e: Exception) {

                _events.send(
                    VerificationEvent.Error(
                        "Código inválido"
                    )
                )

            } finally {

                uiState = uiState.copy(isLoading = false)

            }
        }
    }


    fun resend() {

        viewModelScope.launch {

            try {

                // llamar cognito resend

                _events.send(
                    VerificationEvent.Error(
                        "Código reenviado"
                    )
                )

            } catch (e: Exception) {

                _events.send(
                    VerificationEvent.Error(
                        "No se pudo reenviar"
                    )
                )
            }
        }
    }
}

