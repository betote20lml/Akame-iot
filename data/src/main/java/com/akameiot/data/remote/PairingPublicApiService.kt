package com.akameiot.data.remote

import com.akameiot.data.remote.dto.ConsumeTokenDto
import com.akameiot.data.remote.dto.ConsumeTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface PairingPublicApiService {
    @POST("pairing/consume")
    suspend fun consumeToken(
        @Body body: ConsumeTokenRequest,
    ): ConsumeTokenDto
}