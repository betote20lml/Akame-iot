package com.akameiot.app.ui.home

import com.akameiot.app.ui.home.model.ChartPointsKey
import com.akameiot.app.ui.home.model.NodeTelemetryUiModel
import com.akameiot.app.ui.home.model.TelemetryUiModel
import com.akameiot.domain.model.AppUser
import com.akameiot.domain.model.Network
import com.akameiot.app.ui.home.model.ChartUiModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val telemetry: List<TelemetryUiModel> = emptyList(),
    val appUser: AppUser? = null,
    val selectedNetwork: TelemetryUiModel? = null,
    val selectedNetworkInfo: Network? = null,
    val networks: List<Network> = emptyList(),
    val filterNetworks: List<Network> = emptyList(),
    val networksOrder: List<String> = emptyList(),
    val filterMetrics: List<String> = emptyList(),
    val metricsOrder: List<String> = emptyList(),
    val sortAscending: Boolean? = null,
    val savedFilterNetworkNames: List<String> = emptyList(),
    val visibleNodes: List<NodeTelemetryUiModel> = emptyList(),
    val availableMetrics: List<String> = emptyList(),
    val isEmptyState: Boolean = false,
    val meshWindows: Map<String, Long> = emptyMap(),
    val viewMode: HomeViewMode = HomeViewMode.CARDS,
    val charts: List<ChartUiModel> = emptyList(),
    val chartFromTs: Long = 0L,
    val globalNow: Long = 0L,
    val chartPoints: Map<ChartPointsKey, List<Pair<Long, Double>>> = emptyMap(),
    val chartPointsVersion: Int = 0
)