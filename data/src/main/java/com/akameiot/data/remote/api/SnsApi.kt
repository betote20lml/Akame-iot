package com.akameiot.data.remote.api

import com.akameiot.data.remote.dto.SnsSubscriptionRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SnsApi {
    @POST("devices/subscribe")
    suspend fun subscribeToTopic(
        @Header("Authorization") token: String,
        @Body request: SnsSubscriptionRequestDto,
    )
}