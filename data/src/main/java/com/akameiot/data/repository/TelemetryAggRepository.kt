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
        dao.upsertAggPoint(
            level       = bucket.level,
            meshId      = bucket.meshId,
            nodeId      = bucket.nodeId,
            metric      = bucket.metric,
            bucketStart = bucket.bucketStart,
            firstTs     = bucket.firstTs,
            firstVal    = bucket.firstVal,
            lastTs      = bucket.lastTs,
            lastVal     = bucket.lastVal,
            minTs       = bucket.minTs,
            minVal      = bucket.minVal,
            maxTs       = bucket.maxTs,
            maxVal      = bucket.maxVal,
            count       = bucket.count
        )

    override suspend fun getAggHistory(
        level: String, meshId: String, nodeId: Int, metric: String, fromTs: Long
    ): List<TelemetryAggBucket> =
        dao.getAggHistory(level, meshId, nodeId, metric, fromTs).map { it.toDomain() }

    private fun TelemetryAggEntity.toDomain() = TelemetryAggBucket(
        level, meshId, nodeId, metric, bucketStart,
        firstTs, firstVal, lastTs, lastVal,
        minTs, minVal, maxTs, maxVal, count
    )
}