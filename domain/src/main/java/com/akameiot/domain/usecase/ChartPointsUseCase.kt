package com.akameiot.domain.usecase

import com.akameiot.domain.model.ChartTimeRange
import com.akameiot.domain.model.TelemetryAggBucket
import com.akameiot.domain.repository.TelemetryAggRepository
import com.akameiot.domain.repository.TelemetryRepository

class ChartPointsUseCase(
    private val telemetryRepository : TelemetryRepository,      // datos crudos
    private val aggRepository       : TelemetryAggRepository    // agregados
) {

    suspend fun load(
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long,
        range  : ChartTimeRange
    ): List<Pair<Long, Double>> = when (range) {

        ChartTimeRange.H24 ->
            telemetryRepository.getMetricHistory(meshId, nodeId, metric, fromTs)

        ChartTimeRange.D7 -> loadAgg("7d", meshId, nodeId, metric, fromTs)
        ChartTimeRange.M1 -> loadAgg("1m", meshId, nodeId, metric, fromTs)
        ChartTimeRange.M3 -> loadAgg("3m", meshId, nodeId, metric, fromTs)
    }

    private suspend fun loadAgg(
        level  : String,
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): List<Pair<Long, Double>> =
        aggRepository.getAggHistory(level, meshId, nodeId, metric, fromTs)
            .filter { bucket -> bucket.count > 0 }
            .flatMap { bucket -> bucket.toPoints() }

    private fun TelemetryAggBucket.toPoints(): List<Pair<Long, Double>> {
        if (count == 1) return listOf(Pair(firstTs, firstVal))
        return listOf(
            Pair(firstTs, firstVal),
            Pair(minTs,   minVal),
            Pair(maxTs,   maxVal),
            Pair(lastTs,  lastVal)
        )
            .sortedBy { pair -> pair.first }
            .distinctBy { pair -> pair.first }
    }
}