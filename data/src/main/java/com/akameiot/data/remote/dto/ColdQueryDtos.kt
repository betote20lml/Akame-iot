package com.akameiot.data.remote.dto

data class ColdQueryStartResponse(
    val queryExecutionId: String
)

data class ColdQueryStatusResponse(
    val status: String   // SUCCEEDED | FAILED | CANCELLED | RUNNING
)

data class ColdQueryResultsResponse(
    val items: List<TelemetryDto>,
    val nextToken: String?
)