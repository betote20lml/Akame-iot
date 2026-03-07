package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.exceptions.ActivationCodeInvalidException
import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.usecase.ActivateDeviceUseCase
import com.akameiot.domain.session.AuthSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.akameiot.domain.exceptions.SessionExpiredException
import com.akameiot.domain.model.AppUser
import com.akameiot.domain.usecase.GetAppUserUseCase

class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager,
    private val getAppUserUseCase: GetAppUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = getAppUserUseCase()
                _uiState.update { it.copy(appUser = user) }

                //solo para debug
                val role = when (user) {
                    is AppUser.Owner -> "OWNER"
                    is AppUser.Limited -> "LIMITED"
                }
                _events.emit(HomeEvent.ShowUserRole(role))



            } catch (e: Exception) {
                // Si falla, dejamos appUser null — UI mostrará estado por defecto
            }
        }
    }

    fun onSheetAction(input: String, displayName: String) {
        viewModelScope.launch {
            when (_uiState.value.appUser) {
                is AppUser.Owner -> activateDevice(input, displayName)
                is AppUser.Limited -> _events.emit(HomeEvent.ShowDeviceId(input))
                null -> _events.emit(HomeEvent.ShowError("Usuario no identificado"))
            }
        }
    }

    fun activateDevice(code: String, displayName: String) {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            try {
                val token = authSessionManager.fetchIdToken()

                val response = activateDeviceUseCase(
                    token,
                    DeviceActivationRequest(
                        activationCode = code,
                        displayName = displayName
                    )
                )

                _events.emit(HomeEvent.NavigateToDetails(response.thingName))

            } catch (e: ActivationCodeInvalidException) {
                _events.emit(
                    HomeEvent.ActivationCodeInvalid("Código de activación inválido")
                )

            }catch (e: SessionExpiredException) {
                authSessionManager.logout()
                _events.emit(HomeEvent.NavigateToLogin)

            } catch (e: Exception) {
                _events.emit(
                    HomeEvent.ShowError(
                        e.message ?: "Error activando dispositivo"
                    )
                )
            }finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}