package com.akameiot.app.ui.indexfactory

import com.akameiot.domain.model.AppUser

// ── Historical stats for a single time-range ──────────────────────────────────
data class RangeStats(
    val label: String,
    val min: String,
    val max: String,
)

// ── One row in the list ────────────────────────────────────────────────────────
data class NodeLimitItem(
    val networkName: String,
    val meshId: String,
    val nodeId: Int,
    val nodeName: String,
    val metricKey: String,           // raw key e.g. "humidity"
    val metricDisplayName: String,   // translated e.g. "Humedad"

    val stats: List<RangeStats> = listOf(
        RangeStats("1 día",    "—", "—"),
        RangeStats("7 días",  "—", "—"),
        RangeStats("1 mes",   "—", "—"),
        RangeStats("3 meses", "—", "—"),
        RangeStats("1 año",   "—", "—"),
    ),

)

// ── Screen-level UI state ──────────────────────────────────────────────────────
data class IndexFactoryUiState(
    val appUser: AppUser? = null,
    val isLoading: Boolean = false,

    // Metric submenu
    val selectedMetric: String? = null,
    val selectedMetricDisplay: String? = null,

    // Search
    val searchQuery: String = "",
    val searchActive: Boolean = false,

    val items: List<NodeLimitItem> = emptyList(),
    val visibleItems: List<NodeLimitItem> = emptyList(),
    val editState: Map<Int, Pair<String, String>> = emptyMap(),
)