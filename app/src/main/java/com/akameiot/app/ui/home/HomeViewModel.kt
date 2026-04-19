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
import com.akameiot.domain.usecase.CalculateMeshWindowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.akameiot.app.ui.home.model.ChartPointsKey
import com.akameiot.data.session.GlobalTimeStore
import kotlinx.coroutines.ExperimentalCoroutinesApi


class HomeViewModel(
    private val activateDeviceUseCase: ActivateDeviceUseCase,
    private val authSessionManager: AuthSessionManager,
    private val getAppUserUseCase: GetAppUserUseCase,
    private val networkManager: NetworkManager,
    private val tokenStore: FcmTokenStore,
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
    private val filterPreferencesStore: FilterPreferencesStore,
    private val chartPointsUseCase: com.akameiot.domain.usecase.ChartPointsUseCase,
    private val globalTimeStore: GlobalTimeStore,


    ) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val chartFlowCache =
        mutableMapOf<ChartPointsKey, Flow<List<Pair<Long, Double>>>>()
    private val chartPointsMutable = mutableMapOf<ChartPointsKey, List<Pair<Long, Double>>>()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()



    init {
        validateSession()
        loadUser()
        loadFilterPreferences()
        loadMeshWindows()
        checkPendingFcmResubscribe()
        observeTelemetry()
        observeCharts()
        preloadChartFlows()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCharts() {
        viewModelScope.launch {
            _uiState
                .map { state ->
                    state.visibleNodes.mapNotNull { node ->
                        val metric = node.metrics.firstOrNull()?.name ?: return@mapNotNull null
                        ChartPointsKey(
                            meshId = node.meshId,
                            nodeId = node.nodeId,
                            metric = metric,
                            range  = state.viewMode.chartRange
                        )
                    }
                }
                .distinctUntilChanged()
                .flatMapLatest { keys ->
                    if (keys.isEmpty()) return@flatMapLatest emptyFlow()

                    val range = keys.first().range
                    val fromTsFlow = globalTimeStore.globalNowFlow
                        .map { it - range.seconds }
                        .distinctUntilChanged()

                    // limpiar puntos de keys inactivas
                    val activeKeys = keys.toSet()
                    chartPointsMutable.keys.removeAll { it !in activeKeys }

                    merge(*keys.map { key ->
                        getOrCreateChartFlow(key, fromTsFlow)
                    }.toTypedArray())
                }
                .collect { (key, points) ->
                    chartPointsMutable[key] = points
                    _uiState.update {
                        it.copy(chartPoints = chartPointsMutable.toMap())
                    }
                }
        }
    }

    private suspend fun handleSessionExpired() {
        authSessionManager.logout()
        _events.emit(HomeEvent.NavigateToLogin)
    }

    private fun validateSession() {
        viewModelScope.launch {
            try {
                authSessionManager.fetchIdToken()
            } catch (e: SessionExpiredException) {
                handleSessionExpired()
            } catch (_: Exception) {
            }
        }
    }

    private fun loadFilterPreferences() {
        viewModelScope.launch {
            filterPreferencesStore.prefsFlow.first().let { prefs ->
                _uiState.update { state ->
                    state.copy(
                        networksOrder  = prefs.networksOrder,
                        filterMetrics  = prefs.filterMetrics,
                        metricsOrder   = prefs.metricsOrder,
                        sortAscending  = prefs.sortAscending,
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
                    filterNetworks = s.filterNetworks.map { it.thingName }.distinct(),

                    networksOrder  = s.networksOrder.distinct(),
                    filterMetrics  = s.filterMetrics.distinct(),
                    metricsOrder   = s.metricsOrder.distinct(),
                    sortAscending  = s.sortAscending,
                )
            )
        }
    }

    private fun observeTelemetry() {
        viewModelScope.launch {

            val dataFlow = combine(
                telemetryDao.observeLatestPerMetric(),
                networkStore.networksFlow()
            ) { latestTelemetry, networks ->
                latestTelemetry to networks
            }

            combine(
                dataFlow,
                _uiState
            ) { (latestTelemetry, networks), state ->
                Triple(latestTelemetry, networks, state)
            }
                .map { (latestTelemetry, networks, state) ->

                    withContext(Dispatchers.Default) {


                        val networkNames = networks.associate { it.thingName to it.displayName }
                        val uiTelemetry = latestTelemetry.toUiModel(networkNames)
                        val telemetryById = uiTelemetry.associateBy { it.meshId }

                        val latestGlobalTs = latestTelemetry
                            .maxOfOrNull { it.timestamp } ?: 0L

                        if (latestGlobalTs > 0) {
                            globalTimeStore.setGlobalNow(latestGlobalTs)
                        }



                        val currentSelectedInfo = state.selectedNetworkInfo
                        val newSelectedInfo = currentSelectedInfo ?: networks.firstOrNull()
                        val newSelectedTelemetry = newSelectedInfo?.let {
                            telemetryById[it.thingName]
                        }

                        // Restaurar filtros (sin cambios)
                        val savedNames = state.savedFilterNetworkNames
                        val restoredFilterNetworks = if (savedNames.isNotEmpty()) {
                            networks.filter { it.thingName in savedNames }
                        } else {
                            state.filterNetworks
                        }

                        // networksToShow (optimizado leve: evitar any O(n²))
                        val filterSet = restoredFilterNetworks.map { it.thingName }.toSet()

                        val networksToShow = when {
                            filterSet.isEmpty() -> uiTelemetry

                            state.networksOrder.isEmpty() -> {
                                uiTelemetry.filter { it.meshId in filterSet }
                            }

                            else -> {
                                state.networksOrder
                                    .distinct()
                                    .mapNotNull { telemetryById[it] }
                            }
                        }

                        // Precalculos para evitar recomputar dentro de loops
                        val metricsOrder = state.metricsOrder
                        val filterMetricsSet = state.filterMetrics.toSet()
                        val hasMetricFilter = filterMetricsSet.isNotEmpty()

                        val nowSeconds = System.currentTimeMillis() / 1000L

                        // NODES OPTIMIZADO
                        val nodes = networksToShow
                            .distinctBy { it.meshId }
                            .flatMap { network ->

                                val windowSeconds = state.meshWindows[network.meshId]
                                    ?: CalculateMeshWindowUseCase.DEFAULT_WINDOW_SECONDS
                                val staleThresholdSeconds = windowSeconds * 2

                                network.nodes
                                    .distinctBy { it.nodeId }
                                    .mapNotNull { node ->

                                        val originalMetrics = node.metrics
                                        if (originalMetrics.isEmpty()) return@mapNotNull null

                                        // calcular stale
                                        val latestNodeTs =
                                            originalMetrics.maxOfOrNull { it.timestamp } ?: 0L
                                        val isStaleByTime =
                                            (nowSeconds - latestNodeTs) > staleThresholdSeconds

                                        // ordenar métricas (evita map innecesario si no hay orden)
                                        val orderedMetrics = if (metricsOrder.isEmpty()) {
                                            originalMetrics
                                        } else {
                                            val metricsByName = originalMetrics.associateBy { it.name }
                                            val selected = metricsOrder.mapNotNull { metricsByName[it] }
                                            val rest = originalMetrics.filter { it.name !in metricsOrder }
                                            selected + rest
                                        }

                                        // filtrar métricas
                                        val finalMetrics = if (!hasMetricFilter) {
                                            orderedMetrics
                                        } else {
                                            orderedMetrics.filter { it.name in filterMetricsSet }
                                        }

                                        if (finalMetrics.isEmpty()) return@mapNotNull null

                                        // No recrear si nada cambió
                                        val metricsChanged = finalMetrics !== originalMetrics
                                        val staleChanged = node.isStaleByTime != isStaleByTime

                                        if (!metricsChanged && !staleChanged) {
                                            node
                                        } else {
                                            node.copy(
                                                metrics = finalMetrics,
                                                isStaleByTime = isStaleByTime
                                            )
                                        }
                                    }
                            }
                            // evita string allocation innecesaria
                            .distinctBy { it.networkName to it.nodeId }

                        // sorting
                        val sortedNodes = when (state.sortAscending) {
                            true -> nodes.sortedBy {
                                it.metrics.firstOrNull()?.latestValue ?: Double.MIN_VALUE
                            }

                            false -> nodes.sortedByDescending {
                                it.metrics.firstOrNull()?.latestValue ?: Double.MAX_VALUE
                            }

                            null -> nodes.sortedWith(
                                compareBy({ it.networkName }, { it.nodeId })
                            )
                        }

                        //  availableMetrics optimizado (menos flatMaps encadenados)
                        val availableMetrics = buildSet {
                            uiTelemetry.forEach { network ->
                                network.nodes.forEach { node ->
                                    node.metrics.forEach { add(it.name) }
                                }
                            }
                        }.sorted()

                        //  estado final
                        state.copy(
                            isLoading = false,
                            telemetry = uiTelemetry,
                            networks = networks,
                            selectedNetwork = newSelectedTelemetry,
                            selectedNetworkInfo = newSelectedInfo,
                            filterNetworks = restoredFilterNetworks,
                            savedFilterNetworkNames = emptyList(),
                            visibleNodes = sortedNodes,
                            availableMetrics = availableMetrics,
                            isEmptyState = uiTelemetry.isEmpty(),
                            globalNow = if (latestGlobalTs > 0) latestGlobalTs else state.globalNow
                        )
                    }
                }
                //  más efectivo que distinctUntilChanged plano
                .distinctUntilChangedBy { it.visibleNodes }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    private fun loadMeshWindows() {
        viewModelScope.launch {
            try {
                val stored = AppModule.meshWindowStore.getAllWindows()
                _uiState.update { it.copy(meshWindows = stored) }
            } catch (_: Exception) { }
        }
    }

    fun filterByNetwork(network: Network, selected: Boolean) {
        val current = _uiState.value.filterNetworks.toMutableList()
        val currentOrder = _uiState.value.networksOrder.toMutableList()
        if (selected) {

            if (current.none { it.thingName == network.thingName }) {
                current.add(network)
            }
            if (!currentOrder.contains(network.thingName)) {
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

            } catch (e: SessionExpiredException) {
                handleSessionExpired()

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
            handleSessionExpired()

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
                handleSessionExpired()

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
            } catch (e: SessionExpiredException) {
                handleSessionExpired()

            } catch (e: Exception)  {

            }
        }
    }


    fun setViewMode(mode: HomeViewMode) {
        val fromTs = mode.chartRange.let {
            System.currentTimeMillis() / 1000L - it.seconds
        }
        _uiState.update { it.copy(viewMode = mode, chartFromTs = fromTs) }
    }

    private fun getOrCreateChartFlow(
        key: ChartPointsKey,
        fromTsFlow: Flow<Long>
    ): Flow<Pair<ChartPointsKey, List<Pair<Long, Double>>>> {

        val existing = chartFlowCache[key]
        if (existing != null) {
            return existing.map { key to it }
        }

        val newFlow = chartPointsUseCase.observe(
            key.meshId,
            key.nodeId,
            key.metric,
            fromTsFlow,
            key.range
        )
            .onStart { emit(emptyList()) }
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )

        chartFlowCache[key] = newFlow

        if (chartFlowCache.size > 600) {
            val toRemove = chartFlowCache.size - 600
            val iterator = chartFlowCache.entries.iterator()
            repeat(toRemove) {
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }

        return newFlow.map { key to it }
    }


    private fun preloadChartFlows() {
        viewModelScope.launch {
            _uiState
                .map { it.visibleNodes }
                .distinctUntilChanged()
                .collect { nodes ->
                    val range = com.akameiot.app.ui.home.model.ChartTimeRange.H24
                    val fromTsFlow = globalTimeStore.globalNowFlow
                        .map { it - range.seconds }
                        .distinctUntilChanged()

                    nodes.forEach { node ->
                        val metric = node.metrics.firstOrNull()?.name ?: return@forEach
                        val key = ChartPointsKey(
                            meshId = node.meshId,
                            nodeId = node.nodeId,
                            metric = metric,
                            range  = range
                        )
                        if (key !in chartFlowCache) {
                            android.util.Log.d("PRELOAD", "Precargando key: $key")
                            getOrCreateChartFlow(key, fromTsFlow)
                        } else {
                               android.util.Log.d("PRELOAD", "Key ya en cache: $key")
                        }
                    }
                }
        }
    }


}