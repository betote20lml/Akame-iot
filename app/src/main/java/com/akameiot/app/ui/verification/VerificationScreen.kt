package com.akameiot.app.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.runtime.Composable
import com.akameiot.app.ui.navigation.VerificationType
import androidx.compose.runtime.LaunchedEffect
import com.akameiot.app.ui.navigation.Routes


@Composable
fun VerificationScreen(
    navController: NavController,
    type: VerificationType,
    viewModel: VerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val spacing = LocalSpacing.current
    val state = viewModel.uiState

    // Escuchar eventos UNA sola vez
    LaunchedEffect(Unit) {

        viewModel.events.collect { event ->

            when(event) {

                VerificationEvent.Success -> {

                    navController.navigate(Routes.LANDING) {
                        popUpTo(0)
                    }
                }

                is VerificationEvent.Error -> {
                    // luego podemos usar Snackbar
                }
            }
        }
    }

    AuthScaffold {

        AuthHeader(
            text = when(type) {
                VerificationType.REGISTER -> "Verifica tu correo"
                VerificationType.PASSWORDLESS_LOGIN -> "Accede a tu cuenta"
            }
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = when(type) {

                VerificationType.REGISTER ->
                    "Ingresa el código que enviamos a tu correo para activar tu cuenta. Si no lo ves revisa tu carpeta de spam."

                VerificationType.PASSWORDLESS_LOGIN ->
                    "Ingresa el código que enviamos a tu correo para acceder nuevamente a tu cuenta."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        OtpField(
            onOtpComplete = {
                viewModel.onCodeChange(it)
            }
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        state.error?.let {

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = if (state.isLoading) "Verificando..." else "Continuar",
            enabled = !state.isLoading,
            onClick = {
                viewModel.verify()
            }
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        TextButton(
            onClick = {
                viewModel.resend()
            }
        ) {
            Text("Enviar de nuevo")
        }
    }
}
