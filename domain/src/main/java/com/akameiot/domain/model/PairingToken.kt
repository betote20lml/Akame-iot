package com.akameiot.domain.model

data class PairingToken(
    val token: String,
    val expiresAt: Long,
    val ttlSeconds: Int,
)