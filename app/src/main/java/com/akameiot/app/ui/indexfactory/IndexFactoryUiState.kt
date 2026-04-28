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
    val metricKey: String,
    val metricDisplayName: String,
    val stats: List<RangeStats> = listOf(
        RangeStats("1 día",   "—", "—"),
        RangeStats("7 días",  "—", "—"),
        RangeStats("1 mes",   "—", "—"),
        RangeStats("3 meses", "—", "—"),
        RangeStats("1 año",   "—", "—"),
    ),
    val savedMin: String = "",
    val savedMax: String = "",
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

    val items: List<NodeLimitItemUi> = emptyList(),
    val visibleItems: List<NodeLimitItemUi> = emptyList(),
)

data class NodeLimitItemUi(
    val item: NodeLimitItem,
    val userMin: String,
    val userMax: String,
)