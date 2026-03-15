package com.akameiot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SessionDevicesResponseDto(
    @SerializedName("meshes") val meshes: List<MeshDto>
)

data class MeshDto(
    @SerializedName("thingName")  val thingName: String,
    @SerializedName("displayName") val displayName: String,
)