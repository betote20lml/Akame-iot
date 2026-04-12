package com.akameiot.data.repository

import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.mapper.toEntities
import com.akameiot.data.remote.api.TelemetryApiService
import com.akameiot.data.remote.dto.TelemetryDto
import kotlinx.coroutines.flow.Flow
import com.akameiot.data.local.entity.TelemetryEntity
import com.akameiot.domain.repository.TelemetryRepository as TelemetryRepositoryDomain
import com.akameiot.domain.usecase.AggregateInsertUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

private val bgScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

class TelemetryRepository(
    private val dao: TelemetryDao,
    private val api: TelemetryApiService,
    private val aggregateInsertUseCase: AggregateInsertUseCase,
    private val propagateAggBucketsUseCase: com.akameiot.domain.usecase.PropagateAggBucketsUseCase,
) : TelemetryRepositoryDomain {

    suspend fun getLatestTimestamp(meshId: String): Long =
        dao.getLatestTimestamp(meshId) ?: 0L

    suspend fun fetchAndSaveWindow(
        bearerToken: String,
        meshId: String,
        meshIds: List<String>,
        fromTs: Long
    ) {
        val nowSec = System.currentTimeMillis() / 1000L
        val windowSec = nowSec - fromTs

        if (windowSec < 24 * 3600) {
            val response = api.getRecentTelemetry(
                bearerToken = "Bearer $bearerToken",
                meshId = meshId,
                sinceTs = fromTs
            )
            val entities = response.items.flatMap { it.toEntities() }
            insertAndAggregate(entities)  // ← reemplaza dao.insertAll
        } else {
            coldFetchAndSave(
                bearerToken = bearerToken,
                meshIds = meshIds,
                fromTs = fromTs,
                toTs = nowSec
            )
        }
    }

    private suspend fun coldFetchAndSave(
        bearerToken: String,
        meshIds: List<String>,
        fromTs: Long,
        toTs: Long
    ) {
        val auth = "Bearer $bearerToken"
        val startResp = api.startColdQuery(
            bearerToken = auth,
            meshes = meshIds.joinToString(","),
            fromTs = fromTs,
            toTs = toTs
        )
        val queryId = startResp.queryExecutionId

        var status = ""
        var attempts = 0
        while (status != "SUCCEEDED" && attempts < 300) {
            kotlinx.coroutines.delay(2_000)
            val statusResp = api.getColdQueryStatus(
                bearerToken = auth,
                queryExecutionId = queryId
            )
            status = statusResp.status
            if (status == "FAILED" || status == "CANCELLED")
                throw IllegalStateException("Cold query failed: $status")
            attempts++
        }
        if (status != "SUCCEEDED")
            throw IllegalStateException("Cold query timeout")

        var nextToken: String? = null
        do {
            val page = api.getColdQueryResults(
                bearerToken = auth,
                queryExecutionId = queryId,
                limit = 200,
                nextToken = nextToken
            )
            val entities = page.items.flatMap { it.toEntities() }
            insertAndAggregate(entities)  // ← reemplaza dao.insertAll
            nextToken = page.nextToken
        } while (nextToken != null)
    }

    suspend fun saveTelemetry(dtos: List<TelemetryDto>) {
        val entities = dtos.flatMap { it.toEntities() }
        insertAndAggregate(entities)  // ← reemplaza dao.insertAll
    }

    // ── Punto único de inserción ──────────────────────────────────────────────
    private suspend fun insertAndAggregate(entities: List<TelemetryEntity>) {
        dao.insertAll(entities)

        // Agregación O(1) — síncrona porque es barata por diseño
        entities.forEach { e ->
            aggregateInsertUseCase.insert(
                meshId = e.meshid,
                nodeId = e.nodeId,
                metric = e.metric,
                ts     = e.timestamp,
                value  = e.value
            )
        }

        // Propagación jerárquica — asíncrona, no bloquea el sync
        val series = entities.distinctBy { Triple(it.meshid, it.nodeId, it.metric) }
        bgScope.launch {
            series.forEach { e ->
                propagateAggBucketsUseCase.propagate(
                    meshId = e.meshid,
                    nodeId = e.nodeId,
                    metric = e.metric
                )
            }
        }
    }

    suspend fun cleanOldData(days: Int) {
        val threshold = System.currentTimeMillis() / 1000L - (days * 86400L)
        dao.deleteOlderThan(threshold)
    }

    fun getMetric(meshId: String, nodeId: Int, metric: String): Flow<List<TelemetryEntity>> =
        dao.getMetric(meshId, nodeId, metric)

    override suspend fun getMetricHistory(
        meshId: String, nodeId: Int, metric: String, fromTs: Long
    ): List<Pair<Long, Double>> =
        dao.getMetricHistory(meshId, nodeId, metric, fromTs)
            .map { it.timestamp to it.value }

    override fun observeMetricHistory(
        meshId: String,
        nodeId: Int,
        metric: String,
        fromTs: Long
    ): Flow<List<Pair<Long, Double>>> =
        dao.observeMetricHistory(meshId, nodeId, metric, fromTs)
            .map { list -> list.map { it.timestamp to it.value } }
}