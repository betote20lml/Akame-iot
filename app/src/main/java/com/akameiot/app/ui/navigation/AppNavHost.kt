package com.akameiot.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.akameiot.app.ui.landing.LandingScreen
import com.akameiot.app.ui.login.LoginScreen
import com.akameiot.app.ui.register.RegisterScreen
import com.akameiot.app.ui.terms.TermsScreen
import com.akameiot.app.ui.verification.VerificationScreen
import com.akameiot.app.ui.qrauth.QrAuthScreen

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

        composable(Routes.LANDING) {
            LandingScreen(navController)
        }

        composable(Routes.QR_AUTH) {
            QrAuthScreen(navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Routes.TERMS) {
            TermsScreen(navController)
        }

        //  Ruta con argumento REAL
        composable(
            route = "${Routes.VERIFICATION}/{type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val typeString =
                backStackEntry.arguments?.getString("type")
                    ?: VerificationType.REGISTER.name

            val type = try {
                VerificationType.valueOf(typeString)
            } catch (e: Exception) {
                VerificationType.REGISTER
            }

            VerificationScreen(
                navController = navController,
                type = type
            )
        }
    }
}

