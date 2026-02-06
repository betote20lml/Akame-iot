package com.akameiot.app.ui.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akameiot.app.R
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.components.SecondaryButton
import com.akameiot.coreui.theme.LocalSpacing

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

        /* 🔥 BLOQUE SUPERIOR — LOGO + TAGLINE */

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f) // centra perfecto en cualquier pantalla
        ) {

            Image(
                painter = painterResource(id = R.drawable.akame_logo_text1),
                contentDescription = "Akame Logo",
                modifier = Modifier.size(230.dp) // sweet spot visual
            )

            Spacer(modifier = Modifier.height(spacing.lg))


        }

        /* BLOQUE INFERIOR — BOTONES */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.sm), // aire extra sobre navbar
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {

            PrimaryButton(
                text = "Crear cuenta",
                onClick = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Acceder con token",
                onClick = {
                    // TODO -> navegar al home sin auth
                },
                modifier = Modifier.fillMaxWidth()
            )


            SecondaryButton(
                text = "Iniciar sesión",
                onClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}
