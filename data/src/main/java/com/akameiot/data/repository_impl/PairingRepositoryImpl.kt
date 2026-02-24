package com.akameiot.data.repository_impl

import com.akameiot.data.remote.PairingApiService
import com.akameiot.domain.model.PairingToken
import com.akameiot.domain.usecase.GeneratePairingTokenUseCase

class GeneratePairingTokenUseCaseImpl(
    private val api: PairingApiService
) : GeneratePairingTokenUseCase {
    override suspend fun invoke(idToken: String): PairingToken {
        val dto = api.generateToken("Bearer $idToken")
        return PairingToken(
            token = dto.token,
            expiresAt = dto.expiresAt,
            ttlSeconds = dto.ttlSeconds,
        )
    }
}