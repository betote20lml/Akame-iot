package com.akameiot.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.akameiot.app.ui.home.HomeScreen
import com.akameiot.app.ui.landing.LandingScreen
import com.akameiot.app.ui.login.LoginScreen
import com.akameiot.app.ui.register.RegisterScreen
import com.akameiot.app.ui.terms.TermsScreen
import com.akameiot.app.ui.verification.VerificationScreen
import com.akameiot.app.ui.qrauth.QrAuthScreen
import com.akameiot.app.ui.splash.SplashScreen
import com.akameiot.app.ui.resetpassword.ResetPasswordScreen
import com.akameiot.app.ui.auth.AuthSharedViewModel
import com.akameiot.app.ui.token.PairingTokenScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    val authSharedViewModel: AuthSharedViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()


    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.TOKEN) {
            PairingTokenScreen(navController)
        }

        composable(Routes.LANDING) {
            LandingScreen(navController)
        }

        composable(Routes.QR_AUTH) {
            QrAuthScreen(navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController,
                sharedViewModel = authSharedViewModel
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                navController = navController,
                sharedViewModel = authSharedViewModel
            )
        }

        composable(Routes.TERMS) {
            TermsScreen(navController)
        }

        composable(
            route = Routes.RESET_PASSWORD_WITH_ARG,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val email =
                backStackEntry.arguments?.getString("email")
                    ?: return@composable

            ResetPasswordScreen(
                navController = navController,
                email = email
            )
        }


        composable(Routes.VERIFICATION) {
            VerificationScreen(
                navController = navController,
                sharedViewModel = authSharedViewModel
            )
        }
    }
}

