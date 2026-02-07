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

    // STATE (igual que Register ✅)
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState = _uiState.asStateFlow()

    // EVENTS (mejor que Channel ✅)
    private val _events = MutableSharedFlow<VerificationEvent>()
    val events = _events.asSharedFlow()


    fun onCodeChange(code: String) {
        _uiState.update {
            it.copy(
                code = code,
                error = null
            )
        }
    }


    fun verify() {

        val state = _uiState.value

        if (state.code.length < 6) {

            _uiState.update {
                it.copy(error = "Código incompleto")
            }

            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // 🔥 simulación Cognito
                delay(1500)

                _events.emit(VerificationEvent.Success)

            } catch (e: Exception) {

                _events.emit(
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

                _events.emit(
                    VerificationEvent.Error("Código reenviado")
                )

            } catch (e: Exception) {

                _events.emit(
                    VerificationEvent.Error("No se pudo reenviar")
                )
            }
        }
    }
}


