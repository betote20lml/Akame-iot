package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.akameiot.data.fcm.FcmTokenProvider
import com.akameiot.domain.exceptions.ActivationCodeInvalidException
import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.usecase.ActivateDeviceUseCase
import com.akameiot.domain.session.AuthSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.akameiot.domain.exceptions.SessionExpiredException
import com.akameiot.domain.model.AppUser
import com.akameiot.domain.usecase.GetAppUserUseCase
import com.akameiot.domain.usecase.SubscribeToDeviceTopicUseCase

class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager,
    private val getAppUserUseCase: GetAppUserUseCase,
    private val subscribeToDeviceTopicUseCase: SubscribeToDeviceTopicUseCase, // NUEVO
    private val fcmTokenProvider: FcmTokenProvider,
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

            } catch (e: Exception) {

            }
        }
    }

    fun onSheetAction(input: String, displayName: String) {
        viewModelScope.launch {
            when (_uiState.value.appUser) {
                is AppUser.Owner -> activateDevice(input, displayName)

                is AppUser.Limited -> subscribeToDevice(input)
                null -> _events.emit(HomeEvent.ShowError("Usuario no identificado"))
            }
        }
    }

    private suspend fun subscribeToDevice(thingName: String) {
        try {
            val token = authSessionManager.fetchIdToken()
            val fcmToken = fcmTokenProvider.getToken()
            subscribeToDeviceTopicUseCase(token, thingName, fcmToken)
            _events.emit(HomeEvent.SubscribedToDevice(thingName))
        } catch (e: SessionExpiredException) {
            authSessionManager.logout()
            _events.emit(HomeEvent.NavigateToLogin)
        } catch (e: Exception) {
            _events.emit(HomeEvent.ShowError(e.message ?: "Error suscribiendo dispositivo"))
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
                val fcmToken = fcmTokenProvider.getToken()
                subscribeToDeviceTopicUseCase(token, response.thingName, fcmToken)
                _events.emit(HomeEvent.SubscribedToDevice(response.thingName))


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