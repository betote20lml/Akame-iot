package com.akameiot.app.ui.indexfactory

import com.akameiot.domain.model.AppUser

// ── Historical stats for a single time-range ──────────────────────────────────
data class RangeStats(
    val label: String,
    val min: Double?,
    val max: Double?,
)

// ── One row in the list ────────────────────────────────────────────────────────
data class NodeLimitItem(
    val networkName: String,
    val meshId: String,
    val nodeId: Int,
    // Display name: "<NetworkName> · <nodeId>" — also used for search
    val nodeName: String,
    val metricKey: String,           // raw key e.g. "humidity"
    val metricDisplayName: String,   // translated e.g. "Humedad"

    val stats: List<RangeStats> = listOf(
        RangeStats("7 días",  null, null),
        RangeStats("1 mes",   null, null),
        RangeStats("3 meses", null, null),
        RangeStats("1 año",   null, null),
    ),

    val userMin: String = "",
    val userMax: String = "",
)

// ── Screen-level UI state ──────────────────────────────────────────────────────
data class IndexFactoryUiState(
    val appUser: AppUser? = null,
    val isLoading: Boolean = false,

    // Metric submenu
    val selectedMetric: String? = null,        // raw key e.g. "humidity"
    val selectedMetricDisplay: String? = null, // translated e.g. "Humedad"

    // Search
    val searchQuery: String = "",
    val searchActive: Boolean = false,

    val items: List<NodeLimitItem> = emptyList(),
    val visibleItems: List<NodeLimitItem> = emptyList(),
)