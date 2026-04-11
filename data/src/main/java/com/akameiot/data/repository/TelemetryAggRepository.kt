package com.akameiot.data.repository

import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.local.entity.TelemetryAggEntity
import com.akameiot.domain.model.TelemetryAggBucket
import com.akameiot.domain.repository.TelemetryAggRepository

class TelemetryAggRepositoryImpl(
    private val dao: TelemetryDao
) : TelemetryAggRepository {

    override suspend fun getAggBucket(
        level: String, meshId: String, nodeId: Int, metric: String, bucketStart: Long
    ): TelemetryAggBucket? =
        dao.getAggBucket(level, meshId, nodeId, metric, bucketStart)?.toDomain()

    override suspend fun upsertAggBucket(bucket: TelemetryAggBucket) =
        dao.upsertAgg(bucket.toEntity())

    override suspend fun getAggHistory(
        level: String, meshId: String, nodeId: Int, metric: String, fromTs: Long
    ): List<TelemetryAggBucket> =
        dao.getAggHistory(level, meshId, nodeId, metric, fromTs).map { it.toDomain() }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun TelemetryAggEntity.toDomain() = TelemetryAggBucket(
        level, meshId, nodeId, metric, bucketStart, chunkSize,
        firstTs, firstVal, lastTs, lastVal, minTs, minVal, maxTs, maxVal, count
    )

    private fun TelemetryAggBucket.toEntity() = TelemetryAggEntity(
        level, meshId, nodeId, metric, bucketStart, chunkSize,
        firstTs, firstVal, lastTs, lastVal, minTs, minVal, maxTs, maxVal, count
    )
}