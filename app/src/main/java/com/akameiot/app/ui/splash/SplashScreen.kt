package com.akameiot.app.ui.splash


import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akameiot.di.AppModule
import com.akameiot.app.ui.navigation.Routes


@Composable
fun SplashScreen(
    navController: NavController
) {

    val viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory(
            AppModule.checkLocalSessionUseCase
        )
    )

    LaunchedEffect(Unit) {

        viewModel.checkSession { isLoggedIn ->

            if (isLoggedIn) {

                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }

            } else {

                navController.navigate(Routes.LANDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

}