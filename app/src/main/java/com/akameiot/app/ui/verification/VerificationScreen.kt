package com.akameiot.app.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.runtime.Composable
import com.akameiot.app.ui.navigation.VerificationType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.akameiot.app.ui.navigation.Routes
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import com.akameiot.di.AppModule

@Composable
fun VerificationScreen(
    navController: NavController,
    type: VerificationType,
) { val factory = remember {
        VerificationViewModelFactory(
            AppModule.confirmSignUpUseCase,
            AppModule.autoLoginUseCase,
            AppModule.resendConfirmationCodeUseCase
        )
    }

    val viewModel: VerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    val spacing = LocalSpacing.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Escuchar eventos UNA sola vez
    LaunchedEffect(viewModel) {

        viewModel.events.collectLatest { event ->

            when(event) {

                VerificationEvent.Success -> {

                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }

                is VerificationEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                VerificationEvent.CodeResent -> {
                    snackbarHostState.showSnackbar("Código reenviado")
                }
            }
        }
    }

        AuthScaffold (
            snackbarHostState = snackbarHostState
        ){

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
                value = state.code,
                onValueChange = viewModel::onCodeChange
            )

        Spacer(modifier = Modifier.height(spacing.sm))



        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = if (state.isLoading) "Verificando..." else "Continuar",
            enabled = state.code.length == 6 && !state.isLoading,
            onClick = {
                viewModel.verify()
            }
        )

        Spacer(modifier = Modifier.height(spacing.lg))

            TextButton(
                enabled = state.canResend,
                onClick = { viewModel.resend() }
            ) {
                if (state.resendCooldown > 0) {
                    Text("Reenviar en ${state.resendCooldown}s")
                } else {
                    Text("Enviar de nuevo")
                }
            }
    }
}
