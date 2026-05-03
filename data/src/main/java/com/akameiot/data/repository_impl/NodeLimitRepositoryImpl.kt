package com.akameiot.data.repository_impl

import com.akameiot.data.local.dao.NodeLimitDao
import com.akameiot.data.local.entity.NodeLimitEntity
import com.akameiot.data.remote.api.ExportRequest
import com.akameiot.data.remote.api.NodeLimitApiService
import com.akameiot.data.remote.api.NodeLimitPayload
import com.akameiot.domain.model.NodeLimit
import com.akameiot.domain.repository.NodeLimitRepository
import com.akameiot.domain.session.AuthSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NodeLimitRepositoryImpl(
    private val dao        : NodeLimitDao,
    private val api        : NodeLimitApiService,   // ← nuevo
    private val authSession: AuthSessionManager,    // ← nuevo
) : NodeLimitRepository {

    // ── Métodos existentes — sin cambios ──────────────────────────────────────

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

    // ── Export ────────────────────────────────────────────────────────────────

    override suspend fun exportToCloud(meshId: String) = withContext(Dispatchers.IO) {
        val token = authSession.fetchIdToken()
        val local = dao.getAllByMesh(meshId)

        api.exportLimits(
            bearer = "Bearer $token",
            body   = ExportRequest(
                meshId = meshId,
                limits = local.map { entity ->
                    NodeLimitPayload(
                        nodeId    = entity.nodeId,
                        metric    = entity.metric,
                        userMin   = entity.userMin,
                        userMax   = entity.userMax,
                        updatedAt       = entity.updatedAt,
                    )
                }
            )
        )
        Unit

    }

    // ── Pull ──────────────────────────────────────────────────────────────────

    override suspend fun pullFromCloud(meshId: String) = withContext(Dispatchers.IO) {
        val token    = authSession.fetchIdToken()
        val response = api.pullLimits("Bearer $token", meshId)

        dao.deleteAllByMesh(meshId)
        dao.upsertAll(
            response.limits.map { payload ->
                NodeLimitEntity(
                    meshId    = meshId,
                    nodeId    = payload.nodeId,
                    metric    = payload.metric,
                    userMin   = payload.userMin,
                    userMax   = payload.userMax,
                    updatedAt = payload.clientUpdatedAt,
                )
            }
        )
    }

    // Mappers

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
        updatedAt = updatedAt,
    )
}