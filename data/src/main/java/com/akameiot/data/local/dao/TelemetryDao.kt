package com.akameiot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    AND timestamp >= :fromTimestamp
    ORDER BY timestamp ASC
""")
    suspend fun getMetricHistory(
        meshId: String,
        nodeId: Int,
        metric: String,
        fromTimestamp: Long
    ): List<TelemetryEntity>


}