package com.akameiot.app.ui.home.model

enum class ChartTimeRange(val label: String, val seconds: Long) {
    H24("24h",  86_400L),
    D7 ("7d",   604_800L),
    M1 ("1m",   2_592_000L),
    M3 ("3m",   7_776_000L)
}
