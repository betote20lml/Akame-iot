package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.akameiot.data.network.NetworkManager
import com.akameiot.domain.exceptions.ActivationCodeInvalidException
import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.usecase.ActivateDeviceUseCase
import com.akameiot.domain.session.AuthSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.akameiot.domain.exceptions.SessionExpiredException
import com.akameiot.domain.usecase.GetAppUserUseCase
import com.akameiot.domain.validation.DeviceInput
import com.akameiot.domain.validation.DeviceInputParser
import com.akameiot.domain.policy.DevicePermissions
import com.akameiot.data.session.FcmTokenStore
import com.akameiot.di.AppModule

class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager,
    private val getAppUserUseCase: GetAppUserUseCase,
    private val networkManager: NetworkManager,
    private val tokenStore: FcmTokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUser()
        checkPendingFcmResubscribe()
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

            val user = _uiState.value.appUser

            if (user == null) {
                _events.emit(HomeEvent.ShowError("Usuario no identificado"))
                return@launch
            }

            when (val parsed = DeviceInputParser.parse(input)) {

                is DeviceInput.ActivationCode -> {

                    if (!DevicePermissions.canActivateDevice(user)) {

                        _events.emit(
                            HomeEvent.ShowError(
                                "No tienes permisos para activar dispositivos"
                            )
                        )

                        return@launch
                    }

                    activateDevice(parsed.value, displayName)
                }

                is DeviceInput.ThingName -> {

                    if (!DevicePermissions.canLinkDevice(user)) {

                        _events.emit(
                            HomeEvent.ShowError(
                                "No tienes permisos para conectar dispositivos"
                            )
                        )

                        return@launch
                    }

                    subscribeToDevice(parsed.value, displayName)
                }

                DeviceInput.Invalid -> {

                    _events.emit(
                        HomeEvent.ShowError(
                            "Formato inválido. Usa un código de activación o ID de red"
                        )
                    )
                }
            }
        }
    }

    private suspend fun subscribeToDevice(thingName: String, displayName: String) {

        _uiState.update { it.copy(isLoading = true) }

        try {

            val token = authSessionManager.fetchIdToken()

            networkManager.subscribeNetwork(
                token,
                thingName,
                displayName.ifBlank { thingName }
            )

            // Sync inicial de 3 días después de suscripción exitosa
            AppModule.syncRecentTelemetryUseCase.forceSync(thingName)

            _events.emit(HomeEvent.SubscribedToDevice(thingName))

        } catch (e: SessionExpiredException) {

            authSessionManager.logout()
            _events.emit(HomeEvent.NavigateToLogin)

        } catch (e: Exception) {

            _events.emit(
                HomeEvent.ShowError(
                    e.message ?: "Error suscribiendo dispositivo"
                )
            )

        } finally {

            _uiState.update { it.copy(isLoading = false) }

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
                networkManager.subscribeNetwork(
                    token,
                    response.thingName,
                    displayName
                )

                AppModule.syncRecentTelemetryUseCase.forceSync(response.thingName)

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
    private fun checkPendingFcmResubscribe() {
        viewModelScope.launch {
            if (!tokenStore.needsResubscribe()) return@launch
            try {
                val authToken = authSessionManager.fetchIdToken()
                networkManager.resubscribeAll(authToken)
                tokenStore.clearResubscribeFlag()

                // Sync inicial de 3 días por cada red después de resubscribe exitoso
                AppModule.networkStore.getNetworks().forEach { network ->
                    launch {
                        AppModule.syncRecentTelemetryUseCase.forceSync(network.thingName)
                    }
                }
            } catch (_: Exception) {

            }
        }
    }
}