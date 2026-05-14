package com.akameiot.app.ui.data

sealed interface DataEvent {
    data class ShowError(val message: String) : DataEvent
    data class ExportSuccess(val path: String) : DataEvent
    data object RecoverySuccess : DataEvent
}