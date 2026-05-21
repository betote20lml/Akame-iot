package com.akameiot.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.data.remote.RemoteConfigService
import com.akameiot.di.AppModule
import com.akameiot.domain.usecase.CheckLocalSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.pm.PackageInfoCompat

class SplashViewModel(
    private val checkLocalSessionUseCase: CheckLocalSessionUseCase,
    private val remoteConfigService: RemoteConfigService
) : ViewModel() {

    private val _state =
        MutableStateFlow<SplashState>(
            SplashState.Loading
        )

    val state: StateFlow<SplashState> = _state

    init {

        validateApp()
    }

    private fun validateApp() {

        viewModelScope.launch {

            try {

                remoteConfigService.fetchAndActivate()

            } catch (_: Exception) {

                // Si Firebase falla,
                // continuar normalmente
            }

            val minVersion =
                remoteConfigService
                    .getMinSupportedVersion()

            val forceUpdate =
                remoteConfigService
                    .isForceUpdateEnabled()

            val currentVersion = try {

                val packageInfo =
                    AppModule.appContext.packageManager
                        .getPackageInfo(
                            AppModule.appContext.packageName,
                            0
                        )

                PackageInfoCompat
                    .getLongVersionCode(packageInfo)
                    .toInt()

            } catch (_: Exception) {

                1
            }

            val needsUpdate =
                forceUpdate &&
                        currentVersion < minVersion

            if (needsUpdate) {

                _state.value =
                    SplashState.RequiresUpdate

                return@launch
            }

            val isLoggedIn =
                checkLocalSessionUseCase()

            _state.value =
                if (isLoggedIn) {
                    SplashState.LoggedIn
                } else {
                    SplashState.NotLoggedIn
                }
        }
    }
}