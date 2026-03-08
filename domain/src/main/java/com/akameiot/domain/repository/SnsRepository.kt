package com.akameiot.domain.repository

import com.akameiot.domain.model.SnsSubscriptionRequest

interface SnsRepository {
    suspend fun subscribeToDeviceTopic(
        token: String,
        request: SnsSubscriptionRequest,
    )
}