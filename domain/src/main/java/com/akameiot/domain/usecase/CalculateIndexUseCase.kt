package com.akameiot.domain.usecase

import com.akameiot.domain.model.NodeLimit
import com.akameiot.domain.repository.NodeLimitRepository

data class TelemetryPoint(
    val meshId    : String,
    val nodeId    : Int,
    val timestamp : Long,
    val metric    : String,
    val value     : Double,
)

class CalculateIndexUseCase(
    private val nodeLimitRepository: NodeLimitRepository,
) {

    suspend fun calculate(points: List<TelemetryPoint>): List<TelemetryPoint> {
        if (points.isEmpty()) return emptyList()

        val groups = points.groupBy { it.meshId to it.metric }
        val result = mutableListOf<TelemetryPoint>()

        groups.forEach { (key, items) ->
            val (meshId, metric) = key

            val limits = nodeLimitRepository.getLimitsForMetric(meshId, metric)
            if (limits.isEmpty()) return@forEach

            val limitsMap: Map<Int, NodeLimit> = limits.associateBy { it.nodeId }

            items.forEach { point ->
                val limit = limitsMap[point.nodeId] ?: return@forEach
                val min   = limit.userMin            ?: return@forEach
                val max   = limit.userMax            ?: return@forEach
                if (max == min) return@forEach

                val index = (point.value - min) / (max - min)

                result.add(
                    TelemetryPoint(
                        meshId    = point.meshId,
                        nodeId    = point.nodeId,
                        timestamp = point.timestamp,
                        metric    = "${metric}_index",
                        value     = index,
                    )
                )
            }
        }

        return result
    }
}