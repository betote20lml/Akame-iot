package com.akameiot.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akameiot.app.ui.login.LoginScreen
import com.akameiot.app.ui.register.RegisterScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.REGISTER,
        modifier = modifier
    ) {

        /* ───────────── REGISTER (HOME) ───────────── */

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegister = {
                    // TODO: ir a la app principal
                },
                onGuestLogin = {
                    // TODO: entrar como invitado
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        /* ───────────── LOGIN (SECUNDARIO) ───────────── */

        composable(Routes.LOGIN) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
