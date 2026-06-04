package com.akameiot.domain.usecase

import com.akameiot.domain.model.TelemetryAggBucket
import com.akameiot.domain.repository.TelemetryAggRepository

class AggregateInsertUseCase(private val repository: TelemetryAggRepository) {

    companion object {
        val LEVELS: Map<String, Long> = linkedMapOf(
            "7d" to 10_800L,
            "1m" to 21_600L,
            "3m" to 86_400L,
            "1y" to 604_800L
        )
    }

    suspend fun insert(
        meshId : String,
        nodeId : Int,
        metric : String,
        ts     : Long,
        value  : Double
    ) {
        LEVELS.forEach { (level, chunkSize) ->
            val bucketStart = (ts / chunkSize) * chunkSize
            val existing = repository.getAggBucket(level, meshId, nodeId, metric, bucketStart)

            val updated = existing?.copy(
                lastTs  = ts,
                lastVal = value,
                minTs   = if (value < existing.minVal) ts else existing.minTs,
                minVal  = minOf(value, existing.minVal),
                maxTs   = if (value > existing.maxVal) ts else existing.maxTs,
                maxVal  = maxOf(value, existing.maxVal),
                count   = existing.count + 1
            ) ?: TelemetryAggBucket(
                level       = level,
                meshId      = meshId,
                nodeId      = nodeId,
                metric      = metric,
                bucketStart = bucketStart,
                firstTs     = ts, firstVal = value,
                lastTs      = ts, lastVal  = value,
                minTs       = ts, minVal   = value,
                maxTs       = ts, maxVal   = value,
                count       = 1
            )
            repository.upsertAggBucket(updated)
        }
    }

    suspend fun insertBatch(points: List<TelemetryPoint>) {

        data class BucketKey(
            val level: String,
            val meshId: String,
            val nodeId: Int,
            val metric: String,
            val bucketStart: Long
        )

        // Construir el estado deseado de cada bucket en memoria
        val incomingBuckets = mutableMapOf<BucketKey, TelemetryAggBucket>()

        points.forEach { point ->
            LEVELS.forEach { (level, chunkSize) ->
                val bucketStart = (point.timestamp / chunkSize) * chunkSize
                val key = BucketKey(level, point.meshId, point.nodeId, point.metric, bucketStart)

                val existing = incomingBuckets[key]
                incomingBuckets[key] = existing?.copy(
                    firstTs  = if (point.timestamp < existing.firstTs) point.timestamp else existing.firstTs,
                    firstVal = if (point.timestamp < existing.firstTs) point.value else existing.firstVal,
                    lastTs   = if (point.timestamp > existing.lastTs) point.timestamp else existing.lastTs,
                    lastVal  = if (point.timestamp > existing.lastTs) point.value else existing.lastVal,
                    minTs    = if (point.value < existing.minVal) point.timestamp else existing.minTs,
                    minVal   = minOf(point.value, existing.minVal),
                    maxTs    = if (point.value > existing.maxVal) point.timestamp else existing.maxTs,
                    maxVal   = maxOf(point.value, existing.maxVal),
                    count    = existing.count + 1
                ) ?: TelemetryAggBucket(
                    level       = level,
                    meshId      = point.meshId,
                    nodeId      = point.nodeId,
                    metric      = point.metric,
                    bucketStart = bucketStart,
                    firstTs     = point.timestamp, firstVal = point.value,
                    lastTs      = point.timestamp, lastVal  = point.value,
                    minTs       = point.timestamp, minVal   = point.value,
                    maxTs       = point.timestamp, maxVal   = point.value,
                    count       = 1
                )
            }
        }

        incomingBuckets.forEach { (key, incoming) ->
            val existing = repository.getAggBucket(
                key.level, key.meshId, key.nodeId, key.metric, key.bucketStart
            )
            val merged = existing?.let { mergeWithExisting(incoming, it) } ?: incoming
            repository.upsertAggBucket(merged)
        }
    }

    private fun mergeWithExisting(
        incoming: TelemetryAggBucket,
        existing: TelemetryAggBucket
    ): TelemetryAggBucket {
        return existing.copy(
            firstTs  = if (incoming.firstTs < existing.firstTs) incoming.firstTs else existing.firstTs,
            firstVal = if (incoming.firstTs < existing.firstTs) incoming.firstVal else existing.firstVal,
            lastTs   = if (incoming.lastTs > existing.lastTs) incoming.lastTs else existing.lastTs,
            lastVal  = if (incoming.lastTs > existing.lastTs) incoming.lastVal else existing.lastVal,
            minTs    = if (incoming.minVal < existing.minVal) incoming.minTs else existing.minTs,
            minVal   = minOf(incoming.minVal, existing.minVal),
            maxTs    = if (incoming.maxVal > existing.maxVal) incoming.maxTs else existing.maxTs,
            maxVal   = maxOf(incoming.maxVal, existing.maxVal),
            count    = existing.count + incoming.count
        )
    }
}