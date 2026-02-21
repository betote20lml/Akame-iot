package com.akameiot.app.ui.home

sealed interface HomeEvent {
    data class ShowError(val message: String) : HomeEvent
    object NavigateToDetails : HomeEvent
}