package com.akameiot.app.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME   = "home"
    const val LOGIN  = "login"

    const val HOME_WITH_ARG = "home?loginMode={loginMode}"
    fun home(loginMode: String = "") =
        if (loginMode.isEmpty()) "home" else "home?loginMode=$loginMode"

    const val RESET_PASSWORD_WITH_ARG = "reset_password/{email}"
    fun resetPassword(email: String) = "reset_password/$email"

    const val REGISTER   = "register"
    const val TERMS      = "terms"
    const val LANDING    = "landing"
    const val QR_AUTH    = "qr_auth"
    const val VERIFICATION = "verification"
    const val TOKEN      = "token"

    const val DATA = "data"



    const val INDEX_FACTORY_WITH_ARG = "index_factory/{metricKey}"
    fun indexFactory(metricKey: String) = "index_factory/$metricKey"

    const val LOGIN_MODE_RETURNING = "returning"
    const val LOGIN_MODE_NEW       = "new"
    const val LOGIN_MODE_TOKEN     = "token"
}