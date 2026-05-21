package com.akameiot.app.ui.splash

sealed class SplashState {

    data object Loading : SplashState()

    data object RequiresUpdate : SplashState()

    data object LoggedIn : SplashState()

    data object NotLoggedIn : SplashState()
}