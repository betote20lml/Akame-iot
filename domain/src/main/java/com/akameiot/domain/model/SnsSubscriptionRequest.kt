package com.akameiot.domain.model

data class SnsSubscriptionRequest(
    val thingName: String,
    val fcmToken: String,
)