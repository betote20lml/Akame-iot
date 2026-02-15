package com.akameiot.app.session

object AuthTempStorage {
    var email: String? = null
    var password: String? = null

    fun clear() {
        email = null
        password = null
    }
}