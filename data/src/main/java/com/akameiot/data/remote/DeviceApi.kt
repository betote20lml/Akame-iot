package com.akameiot.data.remote

import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.model.DeviceActivationResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeviceApi {

    @POST("activate")
    suspend fun activateDevice(
        @Header("Authorization") authHeader: String,
        @Body request: DeviceActivationRequest
    ): DeviceActivationResponse
}