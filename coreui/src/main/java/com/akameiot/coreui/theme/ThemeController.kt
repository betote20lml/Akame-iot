package com.akameiot.coreui.theme

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeController {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isDark = MutableStateFlow<Boolean?>(null)
    val isDark: StateFlow<Boolean?> = _isDark.asStateFlow()

    fun bind(source: Flow<Boolean>) {
        scope.launch {
            source
                .distinctUntilChanged()
                .collect {
                    _isDark.value = it
                }
        }
    }

    fun toggle(onToggle: suspend (Boolean) -> Unit) {
        val newValue = !(_isDark.value ?: false)

        scope.launch {
            onToggle(newValue)
        }
    }
}