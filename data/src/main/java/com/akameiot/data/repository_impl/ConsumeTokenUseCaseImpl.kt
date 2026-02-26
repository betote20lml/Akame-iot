package com.akameiot.data.repository_impl

import com.akameiot.data.remote.PairingPublicApiService
import com.akameiot.data.remote.dto.ConsumeTokenRequest
import com.akameiot.domain.model.PairingResult
import com.akameiot.domain.usecase.ConsumeTokenUseCase

class ConsumeTokenUseCaseImpl(
    private val api: PairingPublicApiService
) : ConsumeTokenUseCase {
    override suspend fun invoke(idToken: String, token: String): PairingResult {
        val dto = api.consumeToken(ConsumeTokenRequest(token = token))
        return PairingResult(
            ownerSub = dto.ownerSub,
            ownerEmail = dto.ownerEmail,
            pairedAt = dto.pairedAt,

        )
    }
}