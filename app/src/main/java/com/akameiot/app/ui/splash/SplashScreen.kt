package com.akameiot.app.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.di.AppModule

@Composable
fun SplashScreen(
    navController: NavController
) {

    val viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory(
            AppModule.checkLocalSessionUseCase,
            AppModule.remoteConfigService
        )
    )

    val state by viewModel.state.collectAsState()

    when (state) {

        SplashState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        SplashState.RequiresUpdate -> {
            android.util.Log.d(
                "FORCE_UPDATE",
                "APP BLOQUEADA"
            )
        }

        SplashState.LoggedIn -> {

            LaunchedEffect(Unit) {

                navController.navigate(Routes.HOME) {

                    popUpTo(Routes.SPLASH) {
                        inclusive = true
                    }
                }
            }
        }

        SplashState.NotLoggedIn -> {

            LaunchedEffect(Unit) {

                navController.navigate(Routes.LANDING) {

                    popUpTo(Routes.SPLASH) {
                        inclusive = true
                    }
                }
            }
        }
    }
}