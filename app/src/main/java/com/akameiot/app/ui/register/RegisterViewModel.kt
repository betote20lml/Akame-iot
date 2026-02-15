package com.akameiot.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.akameiot.domain.validation.PasswordValidator

class RegisterViewModel (
    private val registerUseCase: RegisterUseCase,
    private val passwordValidator: PasswordValidator
    ) : ViewModel() {

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

                val result = registerUseCase(
                    state.email,
                    state.password
                )

                sendEvent(
                    RegisterEvent.Success(
                        requiresConfirmation = result.requiresConfirmation
                    )
                )

            } catch (e: Exception) {

                val message = when {
                    e.message?.contains("exists", true) == true ->
                        "El correo ya está registrado"

                    e.message?.contains("password", true) == true ->
                        "La contraseña no cumple los requisitos"

                    else ->
                        "No se pudo crear la cuenta"
                }
                sendEvent(RegisterEvent.Error(message))
            } finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

