package com.akameiot.app.ui.data

data class DataUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,

    // Filtros
    val networks: List<NetworkOption> = emptyList(),
    val selectedNetworkId: String? = null,

    val metrics: List<String> = emptyList(),
    val metricsDisplay: Map<String, String> = emptyMap(),
    val selectedMetric: String? = null,

    val canRecoverHistoricalData: Boolean = false,

)

data class NetworkOption(
    val thingName: String,
    val displayName: String,
)