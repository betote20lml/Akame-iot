package com.akameiot.data.repository_impl

import com.akameiot.data.local.dao.NodeLimitDao
import com.akameiot.data.local.entity.NodeLimitEntity
import com.akameiot.domain.model.NodeLimit
import com.akameiot.domain.repository.NodeLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NodeLimitRepositoryImpl(
    private val dao: NodeLimitDao,
) : NodeLimitRepository {

    override suspend fun upsert(limit: NodeLimit) =
        dao.upsert(limit.toEntity())

    override suspend fun delete(limit: NodeLimit) =
        dao.delete(limit.toEntity())

    override suspend fun getLimit(meshId: String, nodeId: Int, metric: String): NodeLimit? =
        dao.getLimit(meshId, nodeId, metric)?.toDomain()

    override suspend fun getLimitsForMetric(meshId: String, metric: String): List<NodeLimit> =
        dao.getLimitsForMetric(meshId, metric).map { it.toDomain() }

    override suspend fun getAll(): List<NodeLimit> =
        dao.getAll().map { it.toDomain() }

    override fun observeLimitsForMetric(meshId: String, metric: String): Flow<List<NodeLimit>> =
        dao.observeLimitsForMetric(meshId, metric).map { list -> list.map { it.toDomain() } }

    private fun NodeLimit.toEntity() = NodeLimitEntity(
        meshId    = meshId,
        nodeId    = nodeId,
        metric    = metric,
        userMin   = userMin,
        userMax   = userMax,
        updatedAt = System.currentTimeMillis() / 1000L,
    )

    private fun NodeLimitEntity.toDomain() = NodeLimit(
        meshId  = meshId,
        nodeId  = nodeId,
        metric  = metric,
        userMin = userMin,
        userMax = userMax,
    )
}