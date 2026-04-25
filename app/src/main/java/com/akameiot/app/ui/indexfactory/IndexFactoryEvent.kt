package com.akameiot.app.ui.indexfactory

sealed interface IndexFactoryEvent {
    data class ShowError(val message: String) : IndexFactoryEvent
    object ExportChanges : IndexFactoryEvent
    object RecoverFromCloud : IndexFactoryEvent
    data class LimitSaved(val nodeName: String) : IndexFactoryEvent
}