package com.akameiot.app.ui.data

data class DataUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,

    // Filtros
    val networks: List<NetworkOption> = emptyList(),
    val selectedNetworkId: String? = null,   // null = todas

    val metrics: List<String> = emptyList(),
    val metricsDisplay: Map<String, String> = emptyMap(),
    val selectedMetric: String? = null,      // null = todas

    // Info
    val rowCount: Long = 0L,
)

data class NetworkOption(
    val thingName: String,
    val displayName: String,
)