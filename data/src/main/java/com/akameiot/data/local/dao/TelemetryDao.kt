package com.akameiot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.akameiot.data.local.entity.TelemetryAggEntity
import com.akameiot.data.local.entity.TelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<TelemetryEntity>)

    @Query("""
        SELECT * FROM telemetry 
        WHERE meshid = :meshid AND nodeId = :nodeId AND metric = :metric
        ORDER BY timestamp DESC
    """)
    fun getMetric(
        meshid: String,
        nodeId: Int,
        metric: String
    ): Flow<List<TelemetryEntity>>

    @Query("DELETE FROM telemetry WHERE timestamp < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("""
    SELECT MAX(timestamp) FROM telemetry
    WHERE meshid = :meshid
    """)
    suspend fun getLatestTimestamp(meshid: String): Long?


    @Query("""
    SELECT * FROM telemetry t
    WHERE timestamp IN (
        SELECT timestamp FROM telemetry
        WHERE meshid = t.meshid
        AND nodeId = t.nodeId
        AND metric = t.metric
        ORDER BY timestamp DESC
        LIMIT 2
    )
    """)
    fun observeLatestPerMetric(): Flow<List<TelemetryEntity>>

    @Query("""
    SELECT timestamp FROM telemetry
    WHERE meshid = :meshId
    ORDER BY timestamp ASC
    LIMIT 100
""")
    suspend fun getOldestTimestamps(meshId: String): List<Long>

    @Query("""
    SELECT * FROM telemetry
    WHERE meshid = :meshId
    AND nodeId = :nodeId
    AND metric = :metric
    AND timestamp > :fromTimestamp
    ORDER BY timestamp ASC
""")
    suspend fun getMetricHistory(
        meshId: String,
        nodeId: Int,
        metric: String,
        fromTimestamp: Long
    ): List<TelemetryEntity>


    @Query("""
    SELECT * FROM telemetry_agg
    WHERE level = :level
      AND meshId = :meshId
      AND nodeId = :nodeId
      AND metric = :metric
      AND bucketStart = :bucketStart
    LIMIT 1
""")
    suspend fun getAggBucket(
        level       : String,
        meshId      : String,
        nodeId      : Int,
        metric      : String,
        bucketStart : Long
    ): TelemetryAggEntity?

    @Query("""
    SELECT * FROM telemetry_agg
    WHERE level   = :level
      AND meshId  = :meshId
      AND nodeId  = :nodeId
      AND metric  = :metric
      AND bucketStart >= :fromTs
    ORDER BY bucketStart ASC
""")
    suspend fun getAggHistory(
        level  : String,
        meshId : String,
        nodeId : Int,
        metric : String,
        fromTs : Long
    ): List<TelemetryAggEntity>

    @Query("""
    INSERT OR IGNORE INTO telemetry_agg (
        level, meshId, nodeId, metric, bucketStart,
        firstTs, firstVal, lastTs, lastVal,
        minTs, minVal, maxTs, maxVal, count
    )
    VALUES (
        :level, :meshId, :nodeId, :metric, :bucketStart,
        :firstTs, :firstVal, :lastTs, :lastVal,
        :minTs, :minVal, :maxTs, :maxVal, 1
    )
""")
    suspend fun insertAggIfAbsent(
        level       : String,
        meshId      : String,
        nodeId      : Int,
        metric      : String,
        bucketStart : Long,
        firstTs     : Long,
        firstVal    : Double,
        lastTs      : Long,
        lastVal     : Double,
        minTs       : Long,
        minVal      : Double,
        maxTs       : Long,
        maxVal      : Double
    )

    @Query("""
    UPDATE telemetry_agg SET
        firstTs = :firstTs,
        firstVal= :firstVal,
        lastTs  = :lastTs,
        lastVal = :lastVal,
        minTs   = :minTs,
        minVal  = :minVal,
        maxTs   = :maxTs,
        maxVal  = :maxVal,
        count   = :count
    WHERE level       = :level
      AND meshId      = :meshId
      AND nodeId      = :nodeId
      AND metric      = :metric
      AND bucketStart = :bucketStart
    """)
        suspend fun replaceAgg(
            level: String,
            meshId: String,
            nodeId: Int,
            metric: String,
            bucketStart: Long,
            firstTs: Long,
            firstVal: Double,
            lastTs: Long,
            lastVal: Double,
            minTs: Long,
            minVal: Double,
            maxTs: Long,
            maxVal: Double,
            count: Int
        )

    @Transaction
    suspend fun upsertAggPoint(
        level: String,
        meshId: String,
        nodeId: Int,
        metric: String,
        bucketStart: Long,
        firstTs: Long,
        firstVal: Double,
        lastTs: Long,
        lastVal: Double,
        minTs: Long,
        minVal: Double,
        maxTs: Long,
        maxVal: Double,
        count: Int
    ) {
        insertAggIfAbsent(
            level, meshId, nodeId, metric, bucketStart,
            firstTs, firstVal, lastTs, lastVal,
            minTs, minVal, maxTs, maxVal
        )

        replaceAgg(
            level, meshId, nodeId, metric, bucketStart,
            firstTs, firstVal, lastTs, lastVal,
            minTs, minVal, maxTs, maxVal,
            count
        )
    }

}