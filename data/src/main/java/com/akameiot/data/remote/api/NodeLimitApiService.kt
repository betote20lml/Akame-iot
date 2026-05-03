package com.akameiot.data.remote.api

import retrofit2.http.*

data class NodeLimitPayload(
    val nodeId    : Int,
    val metric    : String,
    val userMin   : Double?,
    val userMax   : Double?,
    val updatedAt : Long,
    val clientUpdatedAt : Long = 0L,
)

data class ExportRequest(
    val meshId : String,
    val limits : List<NodeLimitPayload>,
)

data class ExportResponse(val message: String)
data class PullResponse(val limits: List<NodeLimitPayload>)

interface NodeLimitApiService {

    @POST("node-limits/export")
    suspend fun exportLimits(
        @Header("Authorization") bearer: String,
        @Body body: ExportRequest,
    ): ExportResponse

    @GET("node-limits/latest")
    suspend fun pullLimits(
        @Header("Authorization") bearer: String,
        @Query("meshId") meshId: String,
    ): PullResponse
}