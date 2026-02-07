package com.akameiot.app.ui.qrauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QrAuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QrAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<QrAuthEvent>()
    val events = _events.receiveAsFlow()


    fun pasteToken() {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Luego leeremos clipboard
                kotlinx.coroutines.delay(1200)

                _events.send(QrAuthEvent.Success)

            } catch (e: Exception) {

                _events.send(
                    QrAuthEvent.Error("No se pudo leer el token")
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }

            }
        }
    }


    fun onQrScanned(token: String) {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {

                // Validar token
                kotlinx.coroutines.delay(1200)

                _events.send(QrAuthEvent.Success)

            } catch (e: Exception) {

                _events.send(
                    QrAuthEvent.Error("QR inválido")
                )

            } finally {

                _uiState.update { it.copy(isLoading = false) }

            }
        }
    }
}
