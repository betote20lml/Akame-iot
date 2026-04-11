package com.akameiot.domain.model

enum class ChartTimeRange(val seconds: Long) {
    H24(86_400L),
    D7(604_800L),
    M1(2_592_000L),
    M3(7_776_000L),

    Y1(31_536_000L)
}