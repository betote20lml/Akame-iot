package com.akameiot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConsumeTokenDto(
    @SerializedName("owner_sub") val ownerSub: String,
    @SerializedName("owner_email") val ownerEmail: String,
    @SerializedName("session_payload") val sessionPayload: Map<String, Any> = emptyMap(),
    @SerializedName("paired_at") val pairedAt: Long,
)