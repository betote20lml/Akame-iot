package com.akameiot.domain.repository

import com.akameiot.domain.model.NodeLimit
import kotlinx.coroutines.flow.Flow

interface NodeLimitRepository {

    suspend fun upsert(limit: NodeLimit)

    suspend fun delete(limit: NodeLimit)

    suspend fun getLimit(meshId: String, nodeId: Int, metric: String): NodeLimit?

    suspend fun getLimitsForMetric(meshId: String, metric: String): List<NodeLimit>

    suspend fun getAll(): List<NodeLimit>

    fun observeLimitsForMetric(meshId: String, metric: String): Flow<List<NodeLimit>>

    suspend fun exportToCloud(meshId: String)
    suspend fun pullFromCloud(meshId: String)

}