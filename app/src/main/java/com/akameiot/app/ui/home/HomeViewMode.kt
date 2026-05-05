package com.akameiot.app.ui.home

import com.akameiot.app.ui.home.model.ChartTimeRange

enum class HomeViewMode(val chartRange: ChartTimeRange = ChartTimeRange.H24) {
    CARDS,
    CHARTS_24H(ChartTimeRange.H24),
    CHARTS_7D(ChartTimeRange.D7),
    CHARTS_1M(ChartTimeRange.M1),
    CHARTS_3M(ChartTimeRange.M3),

    CHARTS_1Y(ChartTimeRange.Y1)
}

val VIEW_MODE_ORDER = listOf(
    HomeViewMode.CARDS,
    HomeViewMode.CHARTS_24H,
    HomeViewMode.CHARTS_7D,
    HomeViewMode.CHARTS_1M,
    HomeViewMode.CHARTS_3M,
    HomeViewMode.CHARTS_1Y,
)

fun HomeViewMode.next(): HomeViewMode =
    VIEW_MODE_ORDER.getOrElse(VIEW_MODE_ORDER.indexOf(this) + 1) { VIEW_MODE_ORDER.first() }

fun HomeViewMode.previous(): HomeViewMode =
    VIEW_MODE_ORDER.getOrElse(VIEW_MODE_ORDER.indexOf(this) - 1) { VIEW_MODE_ORDER.last() }

fun HomeViewMode.indexIn() = VIEW_MODE_ORDER.indexOf(this)