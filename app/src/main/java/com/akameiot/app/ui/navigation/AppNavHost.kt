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
import com.akameiot.app.ui.verification.VerificationScreen

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



        composable(Routes.REGISTER) {
            RegisterScreen(
                navController = navController,
                onRegister = {
                    navController.navigate(Routes.VERIFICATION) {
                        popUpTo(Routes.REGISTER) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController
            )
        }

        composable(Routes.TERMS) {
            TermsScreen(navController)
        }

        composable(Routes.LANDING) {
            LandingScreen(navController)
        }

        composable(Routes.VERIFICATION) {
            VerificationScreen(navController)
        }

    }
}
