package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.usecase.ActivateDeviceUseCase
import com.akameiot.domain.session.AuthSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    fun activateDevice(code: String, displayName: String?) {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {
                val token = authSessionManager.fetchIdToken()

                val response = activateDeviceUseCase(
                    token,
                    DeviceActivationRequest(
                        activationCode = code,
                        displayName = displayName?.takeIf { it.isNotBlank() }
                    )
                )

                _events.emit(HomeEvent.NavigateToDetails(response.thingName))

            } catch (e: Exception) {

                _events.emit(
                    HomeEvent.ShowError(
                        e.message ?: "Error activando dispositivo"
                    )
                )

            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}