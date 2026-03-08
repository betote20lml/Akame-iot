package com.akameiot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SnsSubscriptionRequestDto(
    @SerializedName("thingName") val thingName: String,
    @SerializedName("fcmToken") val fcmToken: String,
)