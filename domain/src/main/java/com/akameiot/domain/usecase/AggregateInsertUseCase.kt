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

            val updated = if (existing == null) {
                TelemetryAggBucket(
                    level       = level,
                    meshId      = meshId,
                    nodeId      = nodeId,
                    metric      = metric,
                    bucketStart = bucketStart,
                    chunkSize   = chunkSize,
                    firstTs     = ts, firstVal = value,
                    lastTs      = ts, lastVal  = value,
                    minTs       = ts, minVal   = value,
                    maxTs       = ts, maxVal   = value,
                    count       = 1
                )
            } else {
                existing.copy(
                    lastTs  = ts,
                    lastVal = value,
                    minTs   = if (value < existing.minVal) ts else existing.minTs,
                    minVal  = minOf(value, existing.minVal),
                    maxTs   = if (value > existing.maxVal) ts else existing.maxTs,
                    maxVal  = maxOf(value, existing.maxVal),
                    count   = existing.count + 1
                )
            }
            repository.upsertAggBucket(updated)
        }
    }
}