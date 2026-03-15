package com.akameiot.data.remote.api

import com.akameiot.data.remote.dto.SessionDevicesResponseDto
import retrofit2.http.GET
import retrofit2.http.Header

interface SessionApi {
    @GET("devices")
    suspend fun getDevices(
        @Header("Authorization") token: String
    ): SessionDevicesResponseDto
}