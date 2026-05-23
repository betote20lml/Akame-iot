package com.akameiot.data.repository

import android.util.Log
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
    private val calculateIndexUseCase: com.akameiot.domain.usecase.CalculateIndexUseCase,
    private val onDataInserted: () -> Unit = {},
) : TelemetryRepositoryDomain {

    suspend fun coldFetchWindow(
        bearerToken: String,
        meshId: String,
        meshIds: List<String>,
        fromTs: Long,
        toTs: Long,
    ) {
        coldFetchAndSave(
            bearerToken = bearerToken,
            meshIds     = meshIds,
            fromTs      = fromTs,
            toTs        = toTs,
        )
    }

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
            try {
                val response = api.getRecentTelemetry(
                    bearerToken = "Bearer $bearerToken",
                    meshId = meshId,
                    sinceTs = fromTs
                )

                val entities = response.items.flatMap { it.toEntities() }
                insertAndAggregate(entities)

            } catch (e: retrofit2.HttpException) {

                when (e.code()) {
                    401 -> {
                        throw com.akameiot.domain.exceptions.SessionExpiredException()
                    }
                    429 -> {
                        Log.w("Since", "429 rate limit → ignorando (mesh=$meshId)")
                        return
                    }
                    in 500..599 -> {
                        Log.e("Since", "Server error ${e.code()} → ignorando")
                        return
                    }
                    else -> throw e
                }

            }

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
            insertAndAggregate(entities)
            nextToken = page.nextToken
        } while (nextToken != null)
    }

    suspend fun saveTelemetry(dtos: List<TelemetryDto>) {
        val entities = dtos.flatMap { it.toEntities() }
        insertAndAggregate(entities)
    }

    private suspend fun insertAndAggregate(entities: List<TelemetryEntity>) {
        dao.insertAll(entities)

        val points = entities.map { e ->
            com.akameiot.domain.usecase.TelemetryPoint(
                meshId    = e.meshid,
                nodeId    = e.nodeId,
                timestamp = e.timestamp,
                metric    = e.metric,
                value     = e.value,
            )
        }

        // Calcula índices — solo para nodos con límites definidos
        val indexPoints = calculateIndexUseCase.calculate(points)

        // Convierte índices de vuelta a entities e inserta
        val indexEntities = indexPoints.map { p ->
            TelemetryEntity(
                meshid    = p.meshId,
                nodeId    = p.nodeId,
                timestamp = p.timestamp,
                metric    = p.metric,
                value     = p.value,
            )
        }
        if (indexEntities.isNotEmpty()) dao.insertAll(indexEntities)

        // Agrega crudos + índices
        val allEntities = entities + indexEntities
        allEntities.forEach { e ->
            aggregateInsertUseCase.insert(
                meshId = e.meshid,
                nodeId = e.nodeId,
                metric = e.metric,
                ts     = e.timestamp,
                value  = e.value,
            )
        }

        // Propagación jerárquica asíncrona
        val series = allEntities.distinctBy { Triple(it.meshid, it.nodeId, it.metric) }
        bgScope.launch {
            series.forEach { e ->
                propagateAggBucketsUseCase.propagate(
                    meshId = e.meshid,
                    nodeId = e.nodeId,
                    metric = e.metric,
                )
            }
        }
        onDataInserted()
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