package com.akameiot.domain.usecase

import com.akameiot.domain.model.ChartTimeRange
import com.akameiot.domain.model.TelemetryAggBucket
import com.akameiot.domain.repository.TelemetryAggRepository
import com.akameiot.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map




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
        ChartTimeRange.Y1 -> loadAgg("1y", meshId, nodeId, metric, fromTs)
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
            .sortedWith(compareBy({ it.first }, { it.second }))
            .distinctBy { it.first }
    }

    fun observe(
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long,
        range  : ChartTimeRange
    ): Flow<List<Pair<Long, Double>>> = when (range) {

        ChartTimeRange.H24 ->
            telemetryRepository.observeMetricHistory(meshId, nodeId, metric, fromTs)

        ChartTimeRange.D7 -> observeAgg("7d", meshId, nodeId, metric, fromTs)
        ChartTimeRange.M1 -> observeAgg("1m", meshId, nodeId, metric, fromTs)
        ChartTimeRange.M3 -> observeAgg("3m", meshId, nodeId, metric, fromTs)
        ChartTimeRange.Y1 -> observeAgg("1y", meshId, nodeId, metric, fromTs)
    }

    private fun observeAgg(
        level  : String,
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): Flow<List<Pair<Long, Double>>> =
        aggRepository.observeAggHistory(level, meshId, nodeId, metric, fromTs)
            .map { buckets ->
                buckets
                    .filter { it.count > 0 }
                    .flatMap { it.toPoints() }
            }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observe(
        meshId     : String,
        nodeId     : Int,
        metric     : String,
        fromTsFlow : kotlinx.coroutines.flow.Flow<Long>,
        range      : ChartTimeRange
    ): kotlinx.coroutines.flow.Flow<List<Pair<Long, Double>>> =
        fromTsFlow.flatMapLatest { fromTs ->
            observe(meshId, nodeId, metric, fromTs, range)
        }

}