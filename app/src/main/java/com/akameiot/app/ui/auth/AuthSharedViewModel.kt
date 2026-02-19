package com.akameiot.app.ui.auth


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSharedViewModel : ViewModel() {

    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow<String?>(null)
    val password = _password.asStateFlow()

    fun setCredentials(email: String, password: String) {
        _email.value = email
        _password.value = password
    }

    fun clear() {
        _email.value = null
        _password.value = null
    }
}