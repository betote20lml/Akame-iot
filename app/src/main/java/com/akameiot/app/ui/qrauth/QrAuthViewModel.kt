package com.akameiot.app.ui.qrauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.ConsumeTokenUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QrAuthViewModel(
    private val consumeTokenUseCase: ConsumeTokenUseCase,
    private val authSessionManager: AuthSessionManager,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(QrAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QrAuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    fun pasteToken(token: String) {
        consumeToken(token)
    }

    fun onQrScanned(token: String) {
        consumeToken(token)
    }

    private fun consumeToken(token: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Paso 1 — obtener owner_email del backend
                val result = consumeTokenUseCase("", token)

                // Paso 2 — iniciar sesión en Cognito con Custom Auth Flow
                authSessionManager.signInWithCustomAuth(
                    username = result.ownerEmail,
                    token = token
                )

                _events.emit(QrAuthEvent.Success)

            } catch (e: Exception) {
                _events.emit(QrAuthEvent.Error(e.message ?: "Token inválido o expirado"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}