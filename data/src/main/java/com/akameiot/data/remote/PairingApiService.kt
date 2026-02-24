package com.akameiot.data.remote

import com.akameiot.data.remote.dto.PairingTokenDto
import retrofit2.http.Header
import retrofit2.http.POST

interface PairingApiService {
    @POST("pairing/generate")
    suspend fun generateToken(
        @Header("Authorization") bearerToken: String,
    ): PairingTokenDto
}