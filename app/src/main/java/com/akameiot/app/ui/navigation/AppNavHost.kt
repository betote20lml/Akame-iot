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
import android.net.Uri

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

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


        //  Ruta con argumento REAL
        composable(
            route = Routes.VERIFICATION_WITH_ARGS,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val type = VerificationType.valueOf(
                backStackEntry.arguments?.getString("type")!!
            )

            val email = Uri.decode(
                backStackEntry.arguments?.getString("email")!!
            )

            val password = Uri.decode(
                backStackEntry.arguments?.getString("password")!!
            )

            VerificationScreen(
                navController = navController,
                type = type,
                email = email,
                password = password
            )
        }
    }
}

