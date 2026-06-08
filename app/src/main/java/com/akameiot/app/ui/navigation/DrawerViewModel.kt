package com.akameiot.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.formatter.MetricFormatter
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.di.AppModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import com.akameiot.domain.policy.isIndexMetric
import com.akameiot.coreui.components.ConnectionLevel
import com.akameiot.domain.usecase.CalculateMeshWindowUseCase
import kotlinx.coroutines.Dispatchers

data class DrawerUiState(
    val metrics: List<String> = emptyList(),
    val metricsDisplay: Map<String, String> = emptyMap(),
    val connectionStatus: String = "Cargando...",
    val isOnline: Boolean = true,
    val connectionLevel: ConnectionLevel = ConnectionLevel.OK,
)

class DrawerViewModel(
    private val telemetryDao: TelemetryDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    init {
        observeMetrics()
        observeConnectionStatus()
    }

    private fun observeMetrics() {
        viewModelScope.launch {
            telemetryDao.observeLatestPerMetric()
                .map { list ->
                    list
                        .map { it.metric }
                        .filterNot { it.isIndexMetric() }
                        .distinct()
                        .sorted()
                }
                .distinctUntilChanged()
                .collect { keys ->
                    val locale = Locale.getDefault()
                    val display = keys.associateWith {
                        MetricFormatter.formatName(it, locale)
                    }
                    _uiState.update {
                        it.copy(metrics = keys, metricsDisplay = display)
                    }
                }
        }
    }

    fun refreshConnectionStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val networks = try {
                AppModule.networkStore.getNetworks()
            } catch (_: Exception) { emptyList() }

            if (networks.isEmpty()) return@launch

            val nowSeconds = System.currentTimeMillis() / 1000L
            val lastSeen = AppModule.lastSeenPerMesh.value
            var staleCount = 0

            val meshWindows = try {
                AppModule.meshWindowStore.getAllWindows()
            } catch (_: Exception) { emptyMap() }

            networks.forEach { network ->
                val lastTs = lastSeen[network.thingName]
                    ?: AppModule.telemetryDao.getLatestTimestamp(network.thingName)
                    ?: run { staleCount++; return@forEach }

                val windowSeconds = meshWindows[network.thingName]
                val threshold = if (windowSeconds == null) {
                    30L * 60L * 2L
                } else {
                    windowSeconds * 2
                }

                if ((nowSeconds - lastTs) > threshold) staleCount++
            }

            val (connectionStatus, isOnline, level) = when {
                staleCount == 0            -> Triple("Redes actualizadas",       true,  ConnectionLevel.OK)
                staleCount < networks.size -> Triple("Actualización incompleta", false, ConnectionLevel.PARTIAL)
                else                       -> Triple("Redes desactualizadas",    false, ConnectionLevel.OFFLINE)
            }
            _uiState.update { it.copy(connectionStatus = connectionStatus, isOnline = isOnline, connectionLevel = level) }
        }
    }

    private fun observeConnectionStatus() {
        AppModule.networkStatusFlow
            .onEach { status ->
                val (connectionStatus, isOnline, level) = when (status) {
                    AppModule.NetworkStatus.ALL_ONLINE  -> Triple("Redes actualizadas",       true,  ConnectionLevel.OK)
                    AppModule.NetworkStatus.PARTIAL     -> Triple("Actualización incompleta", false, ConnectionLevel.PARTIAL)
                    AppModule.NetworkStatus.ALL_OFFLINE -> Triple("Redes desactualizadas",    false, ConnectionLevel.OFFLINE)
                }
                _uiState.update {
                    it.copy(
                        connectionStatus = connectionStatus,
                        isOnline = isOnline,
                        connectionLevel = level
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

class DrawerViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DrawerViewModel(
            telemetryDao = AppModule.telemetryDao,
        ) as T
}