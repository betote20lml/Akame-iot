package com.akameiot.app.ui.home.model

data class ChartUiModel(
    val nodeId: Int,
    val meshId: String,
    val networkName: String,
    val metricName: String,
    val chartRange: ChartTimeRange = ChartTimeRange.H24,
    val isStale: Boolean = false,
    val isStaleByTime: Boolean = false
)