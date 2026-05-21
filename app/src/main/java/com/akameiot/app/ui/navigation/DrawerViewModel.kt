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