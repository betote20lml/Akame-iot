package com.akameiot.domain.usecase

import com.akameiot.domain.model.PairingToken

interface GeneratePairingTokenUseCase {
    suspend operator fun invoke(idToken: String): PairingToken
}