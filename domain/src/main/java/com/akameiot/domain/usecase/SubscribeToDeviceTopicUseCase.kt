package com.akameiot.domain.usecase

import com.akameiot.domain.model.SnsSubscriptionRequest
import com.akameiot.domain.repository.SnsRepository

class SubscribeToDeviceTopicUseCase(
    private val snsRepository: SnsRepository,
) {
    suspend operator fun invoke(token: String, thingName: String, fcmToken: String) {
        snsRepository.subscribeToDeviceTopic(
            token = token,
            request = SnsSubscriptionRequest(
                thingName = thingName,
                fcmToken = fcmToken,
            )
        )
    }
}