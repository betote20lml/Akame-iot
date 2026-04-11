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

            // Solo busca buckets cerrados dentro de la ventana del nivel destino
            // hacia atrás — evita cargar toda la historia.
            // Una ventana de 2 * dstChunk es suficiente para capturar
            // buckets fuente recién cerrados que aún no se propagaron.
            val lookbackTs = nowTs - (dstChunk * 2)

            val closedBuckets = repository.getAggHistory(
                level  = srcLevel,
                meshId = meshId,
                nodeId = nodeId,
                metric = metric,
                fromTs = lookbackTs
            ).filter { bucket ->
                bucket.bucketStart + srcChunk < nowTs
            }

            if (closedBuckets.isEmpty()) return@forEach

            closedBuckets
                .groupBy { (it.bucketStart / dstChunk) * dstChunk }
                .forEach { (dstBucketStart, sourceBuckets) ->
                    val existing = repository.getAggBucket(
                        dstLevel, meshId, nodeId, metric, dstBucketStart
                    )
                    val merged = mergeBuckets(
                        level       = dstLevel,
                        meshId      = meshId,
                        nodeId      = nodeId,
                        metric      = metric,
                        bucketStart = dstBucketStart,
                        chunkSize   = dstChunk,
                        incoming    = sourceBuckets,
                        existing    = existing
                    )
                    repository.upsertAggBucket(merged)
                }
        }
    }

    private fun mergeBuckets(
        level       : String,
        meshId      : String,
        nodeId      : Int,
        metric      : String,
        bucketStart : Long,
        chunkSize   : Long,
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
            chunkSize   = chunkSize,
            firstTs     = first.firstTs,
            firstVal    = first.firstVal,
            lastTs      = last.lastTs,
            lastVal     = last.lastVal,
            minTs       = min.minTs,
            minVal      = min.minVal,
            maxTs       = max.maxTs,
            maxVal      = max.maxVal,
            count       = all.sumOf { it.count }
        )
    }
}