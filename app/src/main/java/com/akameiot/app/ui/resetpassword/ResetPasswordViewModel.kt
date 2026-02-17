package com.akameiot.app.ui.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.usecase.ConfirmResetPasswordUseCase
import com.akameiot.domain.validation.PasswordValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val passwordValidator: PasswordValidator,
    private val confirmResetPasswordUseCase: ConfirmResetPasswordUseCase,
    private val email: String
) : ViewModel() {

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

                sendEvent(ResetPasswordEvent.Success)

            } catch (e: Exception) {

                sendEvent(
                    ResetPasswordEvent.Error(
                        e.message ?: "Código inválido o expirado"
                    )
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

}