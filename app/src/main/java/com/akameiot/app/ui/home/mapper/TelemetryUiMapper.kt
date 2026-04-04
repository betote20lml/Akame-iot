package com.akameiot.app.ui.home.mapper

import com.akameiot.app.ui.home.model.*
import com.akameiot.data.local.entity.TelemetryEntity
import com.akameiot.app.ui.home.model.MetricTrend

fun List<TelemetryEntity>.toUiModel(
    networkNames: Map<String, String>
): List<TelemetryUiModel> {

    return this
        .groupBy { it.meshid }
        .mapNotNull { (meshId, meshData) ->

            val latestMeshTimestamp = meshData.maxOf { it.timestamp }
            val nodes = meshData
                .groupBy { it.nodeId }
                .toSortedMap()
                .mapNotNull { (nodeId, nodeData) ->

                    val metrics = nodeData
                        .groupBy { it.metric }
                        .toSortedMap()
                        .mapNotNull { (metricName, metricData) ->

                            val sorted = metricData.sortedBy { it.timestamp }
                            val latest = sorted.last()

                            val history = sorted.map {
                                it.timestamp to it.value
                            }
                            val trend = when {
                                sorted.size < 2 -> MetricTrend.FLAT
                                latest.value > sorted[sorted.size - 2].value -> MetricTrend.UP
                                latest.value < sorted[sorted.size - 2].value -> MetricTrend.DOWN
                                else -> MetricTrend.FLAT
                            }

                            MetricUiModel(
                                name = metricName,
                                latestValue = latest.value,
                                timestamp = latest.timestamp,
                                history = history,
                                trend = trend
                            )
                        }

                    if (metrics.isEmpty()) return@mapNotNull null

                    NodeTelemetryUiModel(
                        nodeId = nodeId,
                        meshId = meshId,
                        networkName = networkNames[meshId] ?: meshId,
                        metrics = metrics,
                        isStale = metrics.maxOf { it.timestamp } < latestMeshTimestamp
                    )
                }

            if (nodes.isEmpty()) return@mapNotNull null

            TelemetryUiModel(
                meshId = meshId,
                networkName = networkNames[meshId] ?: meshId,
                nodes = nodes
            )
        }
}