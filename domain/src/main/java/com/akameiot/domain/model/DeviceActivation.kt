package com.akameiot.domain.model

data class DeviceActivationRequest(
    val activationCode: String,
    val displayName: String? = null
)

data class DeviceActivationResponse(
    val status: String,
    val thingName: String,
    val lastRenewalDate: Long,
    val expiresAt: Long,
    val userId: String
)