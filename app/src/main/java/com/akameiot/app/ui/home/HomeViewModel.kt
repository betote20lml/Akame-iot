package com.akameiot.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.akameiot.app.ui.home.mapper.toUiModel
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.network.NetworkManager
import com.akameiot.data.session.DeviceNetworkStore
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
import com.akameiot.data.session.FilterPreferencesStore
import com.akameiot.di.AppModule
import com.akameiot.domain.model.Network

class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager,
    private val getAppUserUseCase: GetAppUserUseCase,
    private val networkManager: NetworkManager,
    private val tokenStore: FcmTokenStore,
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
    private val filterPreferencesStore: FilterPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()



    init {
        loadUser()
        loadFilterPreferences()
        checkPendingFcmResubscribe()
        observeTelemetry()
    }

    private fun loadFilterPreferences() {
        viewModelScope.launch {
            filterPreferencesStore.prefsFlow.first().let { prefs ->
                _uiState.update { state ->
                    // Reconstruir filterNetworks desde los thingNames guardados
                    // (las Network completas las tendremos cuando carguen las redes)
                    state.copy(
                        networksOrder  = prefs.networksOrder,
                        filterMetrics  = prefs.filterMetrics,
                        metricsOrder   = prefs.metricsOrder,
                        sortAscending  = prefs.sortAscending,
                        // filterNetworks se reconstruye en observeTelemetry una vez que lleguen las redes
                        savedFilterNetworkNames = prefs.filterNetworks,
                    )
                }
            }
        }
    }

    private fun persistFilters() {
        viewModelScope.launch {
            val s = _uiState.value
            filterPreferencesStore.save(
                FilterPreferencesStore.FilterPrefs(
                    filterNetworks = s.filterNetworks.map { it.thingName },
                    networksOrder  = s.networksOrder,
                    filterMetrics  = s.filterMetrics,
                    metricsOrder   = s.metricsOrder,
                    sortAscending  = s.sortAscending,
                )
            )
        }
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            combine(
                telemetryDao.observeLatestPerMetric(),
                networkStore.networksFlow()
            ) { latestTelemetry, networks ->
                val networkNames = networks.associate { it.thingName to it.displayName }
                Pair(latestTelemetry.toUiModel(networkNames), networks)
            }
                .distinctUntilChanged()
                .collect { (uiTelemetry, networks) ->

                    val currentSelectedInfo = _uiState.value.selectedNetworkInfo
                    val newSelectedInfo = currentSelectedInfo ?: networks.firstOrNull()
                    val newSelectedTelemetry = uiTelemetry.find { it.meshId == newSelectedInfo?.thingName }

                    // Reconstruir filterNetworks desde los nombres guardados ahora que tenemos las redes
                    val savedNames = _uiState.value.savedFilterNetworkNames
                    val restoredFilterNetworks = if (savedNames.isNotEmpty()) {
                        networks.filter { it.thingName in savedNames }
                    } else {
                        _uiState.value.filterNetworks
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            telemetry = uiTelemetry,
                            networks = networks,
                            selectedNetwork = newSelectedTelemetry,
                            selectedNetworkInfo = newSelectedInfo,
                            filterNetworks = restoredFilterNetworks,
                            savedFilterNetworkNames = emptyList(),
                        )
                    }
                }
        }
    }

    fun selectNetwork(network: Network) {
        val telemetry = _uiState.value.telemetry.find { it.meshId == network.thingName }
        _uiState.update { it.copy(selectedNetwork = telemetry, selectedNetworkInfo = network) }
    }

    fun filterByNetwork(network: Network, selected: Boolean) {
        val current = _uiState.value.filterNetworks.toMutableList()
        val currentOrder = _uiState.value.networksOrder.toMutableList()
        if (selected) {
            if (current.none { it.thingName == network.thingName }) {
                current.add(network)
                currentOrder.add(network.thingName)
            }
        } else {
            current.removeAll { it.thingName == network.thingName }
            currentOrder.remove(network.thingName)
        }
        _uiState.update { it.copy(filterNetworks = current, networksOrder = currentOrder) }
        persistFilters()
    }

    fun moveNetworkUp(thingName: String) {
        val order = _uiState.value.networksOrder.toMutableList()
        val idx = order.indexOf(thingName)
        if (idx > 0) {
            order.add(idx - 1, order.removeAt(idx))
            _uiState.update { it.copy(networksOrder = order) }
            persistFilters()
        }
    }

    fun filterByMetric(metric: String, selected: Boolean) {
        val current = _uiState.value.filterMetrics.toMutableList()
        val currentOrder = _uiState.value.metricsOrder.toMutableList()
        if (selected) {
            if (!current.contains(metric)) {
                current.add(metric)
                currentOrder.add(metric)
            }
        } else {
            current.remove(metric)
            currentOrder.remove(metric)
        }
        _uiState.update { it.copy(filterMetrics = current, metricsOrder = currentOrder) }
        persistFilters()
    }

    fun moveMetricUp(metric: String) {
        val order = _uiState.value.metricsOrder.toMutableList()
        val idx = order.indexOf(metric)
        if (idx > 0) {
            order.add(idx - 1, order.removeAt(idx))
            _uiState.update { it.copy(metricsOrder = order) }
            persistFilters()
        }
    }


    fun setSortAscending(ascending: Boolean?) {
        _uiState.update { it.copy(sortAscending = ascending) }
        persistFilters()
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