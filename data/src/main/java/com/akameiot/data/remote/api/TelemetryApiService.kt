package com.akameiot.data.remote.api


import com.akameiot.data.remote.dto.ColdQueryResultsResponse
import com.akameiot.data.remote.dto.ColdQueryStartResponse
import com.akameiot.data.remote.dto.ColdQueryStatusResponse
import com.akameiot.data.remote.dto.TelemetryHotResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TelemetryApiService {

    @GET("telemetry/since")
    suspend fun getRecentTelemetry(
        @Header("Authorization") bearerToken: String,
        @Query("meshid") meshId: String,
        @Query("since") sinceTs: Long
    ): TelemetryHotResponse

    @POST("telemetry/start")
    suspend fun startColdQuery(
        @Header("Authorization") bearerToken: String,
        @Query("meshes") meshes: String,
        @Query("fromTs") fromTs: Long,
        @Query("toTs") toTs: Long
    ): ColdQueryStartResponse

    @GET("telemetry/status")
    suspend fun getColdQueryStatus(
        @Header("Authorization") bearerToken: String,
        @Query("queryExecutionId") queryExecutionId: String
    ): ColdQueryStatusResponse

    @GET("telemetry/results")
    suspend fun getColdQueryResults(
        @Header("Authorization") bearerToken: String,
        @Query("queryExecutionId") queryExecutionId: String,
        @Query("limit") limit: Int,
        @Query("nextToken") nextToken: String?
    ): ColdQueryResultsResponse
}