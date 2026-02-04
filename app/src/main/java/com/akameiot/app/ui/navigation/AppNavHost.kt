package com.akameiot.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akameiot.app.ui.login.LoginScreen
import com.akameiot.app.ui.register.RegisterScreen
import com.akameiot.app.ui.terms.TermsScreen
import com.akameiot.app.ui.landing.LandingScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LANDING,
        modifier = modifier
    ) {

        /* ───────────── REGISTER (HOME) ───────────── */

        composable(Routes.REGISTER) {
            //popUpTo(Routes.LANDING) {
            //    inclusive = true
            //}
            RegisterScreen(
                navController = navController,
                onRegister = {
                    // TODO
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

        composable(Routes.TERMS) {
            TermsScreen(navController)
        }

        composable(Routes.LANDING) {
            LandingScreen(navController)
        }

    }
}
