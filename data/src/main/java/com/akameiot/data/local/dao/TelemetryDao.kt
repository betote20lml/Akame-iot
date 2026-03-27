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
}