package com.akameiot.data.repository_impl

import com.akameiot.data.remote.api.SnsApi
import com.akameiot.data.remote.dto.SnsSubscriptionRequestDto
import com.akameiot.domain.model.SnsSubscriptionRequest
import com.akameiot.domain.repository.SnsRepository

class SnsRepositoryImpl(
    private val snsApi: SnsApi,
) : SnsRepository {

    override suspend fun subscribeToDeviceTopic(
        token: String,
        request: SnsSubscriptionRequest,
    ) {
        snsApi.subscribeToTopic(
            token = "Bearer $token",
            request = SnsSubscriptionRequestDto(
                thingName = request.thingName,
                fcmToken = request.fcmToken,
            )
        )
    }
}