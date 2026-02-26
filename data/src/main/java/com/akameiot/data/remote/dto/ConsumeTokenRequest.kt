package com.akameiot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConsumeTokenRequest(
    @SerializedName("token") val token: String,
)