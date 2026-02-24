package com.akameiot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PairingTokenDto(
    @SerializedName("token") val token: String,
    @SerializedName("expires_at") val expiresAt: Long,
    @SerializedName("ttl_seconds") val ttlSeconds: Int,
)