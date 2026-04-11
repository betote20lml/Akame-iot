package com.akameiot.app.ui.home.model

data class ChartPointsKey(
    val meshId: String,
    val nodeId: Int,
    val metric: String,
    val range: ChartTimeRange,
)