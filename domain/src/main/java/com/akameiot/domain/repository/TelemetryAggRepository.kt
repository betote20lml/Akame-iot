package com.akameiot.domain.repository

import com.akameiot.domain.model.TelemetryAggBucket
import kotlinx.coroutines.flow.Flow

interface TelemetryAggRepository {

    suspend fun getAggBucket(
        level       : String,
        meshId      : String,
        nodeId      : Int,
        metric      : String,
        bucketStart : Long
    ): TelemetryAggBucket?

    suspend fun upsertAggBucket(bucket: TelemetryAggBucket)

    suspend fun getAggHistory(
        level  : String,
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): List<TelemetryAggBucket>

    fun observeAggHistory(
        level  : String,
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): Flow<List<TelemetryAggBucket>>

}