package com.akameiot.domain.repository

import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {

    suspend fun getMetricHistory(
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): List<Pair<Long, Double>>


    fun observeMetricHistory(
        meshId: String,
        nodeId: Int,
        metric: String,
        fromTs: Long
    ): Flow<List<Pair<Long, Double>>>

}