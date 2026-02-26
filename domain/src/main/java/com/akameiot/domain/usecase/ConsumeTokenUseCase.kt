package com.akameiot.domain.usecase

import com.akameiot.domain.model.PairingResult

interface ConsumeTokenUseCase {
    suspend operator fun invoke(idToken: String, token: String): PairingResult
}