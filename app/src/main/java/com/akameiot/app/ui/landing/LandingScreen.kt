package com.akameiot.app.ui.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akameiot.app.R
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.components.SecondaryButton
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun LandingScreen(
    navController: NavController
) {

    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = spacing.lg, vertical = spacing.md),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //  BLOQUE SUPERIOR — LOGO
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {

            val isDark = isSystemInDarkTheme()

            Image(
                painter = painterResource(
                    id = if (isDark) {
                        R.drawable.logo_dark
                    } else {
                        R.drawable.logo_light
                    }
                ),
                contentDescription = "Akame Logo",
                modifier = Modifier.size(230.dp)
            )

            Spacer(modifier = Modifier.height(spacing.lg))


        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {

            PrimaryButton(
                text = "Crear cuenta",
                onClick = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Iniciar sesión",
                onClick = {
                    navController.navigate(Routes.LOGIN) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Acceder con token",
                onClick = {
                    navController.navigate(Routes.QR_AUTH) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}
