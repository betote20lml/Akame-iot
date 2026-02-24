package com.akameiot.app.ui.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.exceptions.SessionExpiredException
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.GeneratePairingTokenUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.*

class PairingTokenViewModel(
    private val generatePairingTokenUseCase: GeneratePairingTokenUseCase,
    private val authSessionManager: AuthSessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingTokenUiState())
    val uiState: StateFlow<PairingTokenUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PairingTokenEvent>()
    val events = _events.asSharedFlow()

    private var expiresAt: Long = 0L

    init {
        generateToken()
    }

    fun generateToken() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, token = null) }

            try {
                val idToken = authSessionManager.fetchIdToken()
                val token = generatePairingTokenUseCase(idToken)

                expiresAt = System.currentTimeMillis() + (token.ttlSeconds * 1000)

                _uiState.update { it.copy(token = token) }

                startCountdown()

            } catch (e: SessionExpiredException) {
                authSessionManager.logout()
                _events.emit(PairingTokenEvent.NavigateToLogin)
            } catch (e: Exception) {
                _events.emit(
                    PairingTokenEvent.ShowError(
                        e.message ?: "Error generando token"
                    )
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {

            while (true) {

                val secondsLeft =
                    ((expiresAt - System.currentTimeMillis()) / 1000)
                        .coerceAtLeast(0)

                _uiState.update { it.copy(secondsLeft = secondsLeft.toInt()) }

                if (secondsLeft <= 0) {
                    generateToken()
                    break
                }

                delay(1000)
            }
        }
    }
}