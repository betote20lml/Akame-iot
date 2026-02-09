package com.akameiot.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    private suspend fun sendEvent(event: RegisterEvent) {
        _events.emit(event)
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun onTermsAccepted(value: Boolean) {
        _uiState.update { it.copy(acceptedTerms = value) }
    }

    fun register() {

        val state = _uiState.value

        if (state.isLoading) return

        if (!state.isFormValid) {
            viewModelScope.launch {
                sendEvent(RegisterEvent.Error("Completa todos los campos correctamente"))
            }
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Aquí irá Cognito luego

                kotlinx.coroutines.delay(1500) // simulación

                sendEvent(RegisterEvent.Success)

            } catch (e: Exception) {

                sendEvent(RegisterEvent.Error("No se pudo crear la cuenta"))

            } finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

