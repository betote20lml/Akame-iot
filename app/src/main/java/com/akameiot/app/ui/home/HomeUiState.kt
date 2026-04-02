package com.akameiot.app.ui.home

import com.akameiot.app.ui.home.model.TelemetryUiModel
import com.akameiot.domain.model.AppUser
import com.akameiot.domain.model.Network

data class HomeUiState(
    val isLoading: Boolean = false,
    val telemetry: List<TelemetryUiModel> = emptyList(),
    val appUser: AppUser? = null,
    val selectedNetwork: TelemetryUiModel? = null,
    val selectedNetworkInfo: Network? = null,
    val networks: List<Network> = emptyList(),
    val filterNetworks: List<Network> = emptyList(),     // vacío = todas
    val networksOrder: List<String> = emptyList(),       // thingNames en orden preferido
    val filterMetrics: List<String> = emptyList(),       // vacío = todas
    val metricsOrder: List<String> = emptyList(),
    val sortAscending: Boolean = true,
    val savedFilterNetworkNames: List<String> = emptyList(),
)