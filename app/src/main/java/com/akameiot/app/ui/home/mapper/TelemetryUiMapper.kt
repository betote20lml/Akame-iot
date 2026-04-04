package com.akameiot.app.ui.home.mapper

import com.akameiot.app.ui.home.model.*
import com.akameiot.data.local.entity.TelemetryEntity

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

                            MetricUiModel(
                                name = metricName,
                                latestValue = latest.value,
                                timestamp = latest.timestamp,
                                history = history
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