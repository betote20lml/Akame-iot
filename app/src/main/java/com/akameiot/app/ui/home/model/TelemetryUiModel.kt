package com.akameiot.app.ui.home.model


data class TelemetryUiModel(
    val meshId: String,
    val networkName: String,
    val nodes: List<NodeTelemetryUiModel>
)

data class NodeTelemetryUiModel(
    val nodeId: Int,
    val meshId: String,
    val networkName: String,
    val metrics: List<MetricUiModel>,
    val isStale: Boolean = false
)

data class MetricUiModel(
    val name: String,
    val latestValue: Double,
    val timestamp: Long,
    val history: List<Pair<Long, Double>>
)