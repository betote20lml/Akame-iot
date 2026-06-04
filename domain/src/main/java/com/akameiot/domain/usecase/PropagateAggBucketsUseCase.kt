package com.akameiot.domain.usecase

import com.akameiot.domain.model.TelemetryAggBucket
import com.akameiot.domain.repository.TelemetryAggRepository

class PropagateAggBucketsUseCase(
    private val repository: TelemetryAggRepository
) {

    companion object {
        private val PROPAGATION_CHAIN = listOf(
            Triple("7d",  10_800L, "1m"),
            Triple("1m",  21_600L, "3m"),
            Triple("3m",  86_400L, "1y"),
        )
    }

    suspend fun propagate(
        meshId : String,
        nodeId : Int,
        metric : String,
        nowTs  : Long = System.currentTimeMillis() / 1000L
    ) {
        PROPAGATION_CHAIN.forEach { (srcLevel, srcChunk, dstLevel) ->
            val dstChunk = AggregateInsertUseCase.LEVELS[dstLevel] ?: return@forEach

            // Buckets cerrados dentro de una ventana acotada (2 * dstChunk)
            // para no cargar toda la historia en cada llamada
            val lookbackTs = nowTs - (dstChunk * 2)

            val closedBuckets = repository.getAggHistory(
                level  = srcLevel,
                meshId = meshId,
                nodeId = nodeId,
                metric = metric,
                fromTs = lookbackTs
            ).filter { it.bucketStart + srcChunk < nowTs }

            if (closedBuckets.isEmpty()) return@forEach

            closedBuckets
                .groupBy { (it.bucketStart / dstChunk) * dstChunk }
                .forEach { (dstBucketStart, sourceBuckets) ->
                    val existing = repository.getAggBucket(
                        dstLevel, meshId, nodeId, metric, dstBucketStart
                    )
                    // upsertAggBucket es idempotente:
                    // si el bucket destino ya tiene estos valores exactos,
                    // el ON CONFLICT DO UPDATE no produce cambio observable
                    repository.upsertAggBucket(
                        mergeBuckets(
                            level       = dstLevel,
                            meshId      = meshId,
                            nodeId      = nodeId,
                            metric      = metric,
                            bucketStart = dstBucketStart,
                            incoming    = sourceBuckets,
                            existing    = existing
                        )
                    )
                }
        }
    }

    private fun mergeBuckets(
        level       : String,
        meshId      : String,
        nodeId      : Int,
        metric      : String,
        bucketStart : Long,
        incoming    : List<TelemetryAggBucket>,
        existing    : TelemetryAggBucket?
    ): TelemetryAggBucket {
        val all = if (existing != null) incoming + existing else incoming
        val first = all.minByOrNull { it.firstTs }!!
        val last  = all.maxByOrNull { it.lastTs }!!
        val min   = all.minByOrNull { it.minVal }!!
        val max   = all.maxByOrNull { it.maxVal }!!

        return TelemetryAggBucket(
            level       = level,
            meshId      = meshId,
            nodeId      = nodeId,
            metric      = metric,
            bucketStart = bucketStart,
            firstTs     = first.firstTs, firstVal = first.firstVal,
            lastTs      = last.lastTs,   lastVal  = last.lastVal,
            minTs       = min.minTs,     minVal   = min.minVal,
            maxTs       = max.maxTs,     maxVal   = max.maxVal,
            count       = all.sumOf { it.count }
        )
    }

    suspend fun propagateBatch(
        series: List<Triple<String, Int, String>>, // meshId, nodeId, metric
        nowTs: Long = System.currentTimeMillis() / 1000L
    ) {
        PROPAGATION_CHAIN.forEach { (srcLevel, srcChunk, dstLevel) ->
            val dstChunk = AggregateInsertUseCase.LEVELS[dstLevel] ?: return@forEach
            val lookbackTs = nowTs - (dstChunk * 2)

            // Una sola pasada por nivel para todas las series
            series.forEach { (meshId, nodeId, metric) ->
                val closedBuckets = repository.getAggHistory(
                    level  = srcLevel,
                    meshId = meshId,
                    nodeId = nodeId,
                    metric = metric,
                    fromTs = lookbackTs
                ).filter { it.bucketStart + srcChunk < nowTs }

                if (closedBuckets.isEmpty()) return@forEach

                closedBuckets
                    .groupBy { (it.bucketStart / dstChunk) * dstChunk }
                    .forEach { (dstBucketStart, sourceBuckets) ->
                        val existing = repository.getAggBucket(
                            dstLevel, meshId, nodeId, metric, dstBucketStart
                        )
                        repository.upsertAggBucket(
                            mergeBuckets(
                                level       = dstLevel,
                                meshId      = meshId,
                                nodeId      = nodeId,
                                metric      = metric,
                                bucketStart = dstBucketStart,
                                incoming    = sourceBuckets,
                                existing    = existing
                            )
                        )
                    }
            }
        }
    }
}