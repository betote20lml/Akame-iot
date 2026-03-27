package com.akameiot.data.remote.dto

data class TelemetryHotResponse(
    val count: Int,
    val items: List<TelemetryDto>
)