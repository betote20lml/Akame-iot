package com.akameiot.data.local.dao

import androidx.room.*
import com.akameiot.data.local.entity.NodeLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeLimitDao {

    // Escritura

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(limit: NodeLimitEntity)

    @Delete
    suspend fun delete(limit: NodeLimitEntity)

    // Lectura puntual

    @Query("""
        SELECT * FROM node_limits
        WHERE meshId = :meshId AND nodeId = :nodeId AND metric = :metric
        LIMIT 1
    """)
    suspend fun getLimit(
        meshId : String,
        nodeId : Int,
        metric : String,
    ): NodeLimitEntity?

    // Lectura reactiva (para la UI)

    @Query("""
        SELECT * FROM node_limits
        WHERE meshId = :meshId AND metric = :metric
        ORDER BY nodeId ASC
    """)
    fun observeLimitsForMetric(
        meshId : String,
        metric : String,
    ): Flow<List<NodeLimitEntity>>

    // Para el motor de índices: todos los límites de una métrica

    @Query("""
        SELECT * FROM node_limits
        WHERE meshId = :meshId AND metric = :metric
    """)
    suspend fun getLimitsForMetric(
        meshId : String,
        metric : String,
    ): List<NodeLimitEntity>

    // Todos los límites (para exportar / sync)

    @Query("SELECT * FROM node_limits ORDER BY meshId, metric, nodeId")
    suspend fun getAll(): List<NodeLimitEntity>

    @Query("SELECT * FROM node_limits WHERE meshId = :meshId")
    suspend fun getAllByMesh(meshId: String): List<NodeLimitEntity>

    @Query("DELETE FROM node_limits WHERE meshId = :meshId")
    suspend fun deleteAllByMesh(meshId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(limits: List<NodeLimitEntity>)

    @Query("SELECT * FROM node_limits WHERE meshId = :meshId AND metric = :metric")
    suspend fun getAllByMeshAndMetric(meshId: String, metric: String): List<NodeLimitEntity>

    @Query("DELETE FROM node_limits WHERE meshId = :meshId AND metric = :metric")
    suspend fun deleteAllByMeshAndMetric(meshId: String, metric: String)


}