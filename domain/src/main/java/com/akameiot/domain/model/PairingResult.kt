package com.akameiot.domain.model

data class PairingResult(
    val ownerSub: String,
    val ownerEmail: String,
    val pairedAt: Long,
)