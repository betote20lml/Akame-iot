package com.akameiot.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.di.AppModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class DrawerUiState(
    val metrics: List<String> = emptyList(),
    val metricsDisplay: Map<String, String> = emptyMap(),
)

class DrawerViewModel(
    private val telemetryDao: TelemetryDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    init {
        observeMetrics()
    }

    private fun observeMetrics() {
        viewModelScope.launch {
            telemetryDao.observeLatestPerMetric()
                .map { list ->
                    list
                        .map { it.metric }
                        .filterNot { it.endsWith("_index") }
                        .distinct()
                        .sorted()
                }
                .distinctUntilChanged()
                .collect { keys ->
                    val locale = Locale.getDefault()
                    val display = keys.associateWith {
                        TelemetryFormatter.formatName(it, locale)
                    }
                    _uiState.update {
                        it.copy(metrics = keys, metricsDisplay = display)
                    }
                }
        }
    }
}

class DrawerViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DrawerViewModel(
            telemetryDao = AppModule.telemetryDao,
        ) as T
}