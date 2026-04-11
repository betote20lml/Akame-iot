package com.akameiot.app.ui.home

import com.akameiot.app.ui.home.model.ChartTimeRange

enum class HomeViewMode(val chartRange: ChartTimeRange? = null) {
    CARDS,
    CHARTS_24H(ChartTimeRange.H24),
    CHARTS_7D(ChartTimeRange.D7),
    CHARTS_1M(ChartTimeRange.M1),
    CHARTS_3M(ChartTimeRange.M3),

    CHARTS_1Y(ChartTimeRange.Y1)
}