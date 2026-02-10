package com.akameiot.app.ui.qrauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class QrAuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QrAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QrAuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()


    fun pasteToken() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Luego leeremos clipboard
                delay(1200)

                _events.emit(QrAuthEvent.Success)

            } catch (e: Exception) {

                _events.emit(
                    QrAuthEvent.Error("No se pudo leer el token")
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }

            }
        }
    }


    fun onQrScanned(token: String) {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Validar token
                delay(1200)

                _events.emit(QrAuthEvent.Success)

            } catch (e: Exception) {

                _events.emit(
                    QrAuthEvent.Error("QR inválido")
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }

            }
        }
    }
}
