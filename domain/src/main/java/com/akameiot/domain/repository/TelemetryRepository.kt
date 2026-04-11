package com.akameiot.domain.repository

interface TelemetryRepository {

    suspend fun getMetricHistory(
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): List<Pair<Long, Double>>
}