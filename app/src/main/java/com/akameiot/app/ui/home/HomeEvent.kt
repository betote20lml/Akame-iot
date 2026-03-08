package com.akameiot.app.ui.home

sealed interface HomeEvent {
    data class ShowError(val message: String) : HomeEvent
    data class NavigateToDetails(val thingName: String) : HomeEvent

    data class ActivationCodeInvalid(val message: String) : HomeEvent

    data class ShowDeviceId(val deviceId: String) : HomeEvent


    object NavigateToLogin : HomeEvent

    data class SubscribedToDevice(val thingName: String) : HomeEvent
}