package com.akameiot.app.ui.qrauth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.data.remote.isNetworkAvailable
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.ConsumeTokenUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QrAuthViewModel(
    private val consumeTokenUseCase: ConsumeTokenUseCase,
    private val authSessionManager: AuthSessionManager,
    private val app: Application,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QrAuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    fun pasteToken(token: String) = consumeToken(token)
    fun onQrScanned(token: String) = consumeToken(token)

    private fun consumeToken(token: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (!isNetworkAvailable(app)) {
                _events.emit(QrAuthEvent.Error("Sin conexión a internet.\nVerifica tu WiFi o datos móviles."))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, statusMessage = "Conectando...") }
            try {
                val result = consumeTokenUseCase("", token)

                _uiState.update { it.copy(statusMessage = "Autenticando...") }
                authSessionManager.signInWithCustomAuth(
                    username = result.ownerEmail,
                    token = token
                )
                authSessionManager.setLimitedSession(true)

                _events.emit(QrAuthEvent.Success)

            } catch (e: Exception) {
                val errorMsg = buildString {
                    appendLine("${e::class.simpleName}")
                    appendLine("${e.message}")
                    e.cause?.let { appendLine("Cause: ${it.message}") }
                }
                _events.emit(QrAuthEvent.Error(errorMsg))
            } finally {
                _uiState.update { it.copy(isLoading = false, statusMessage = null) }
            }
        }
    }
}