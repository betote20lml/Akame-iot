package com.akameiot.app.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.akameiot.app.ui.navigation.Routes
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import com.akameiot.di.AppModule
import com.akameiot.app.ui.auth.AuthSharedViewModel

@Composable
fun VerificationScreen(
    navController: NavController,
    sharedViewModel: AuthSharedViewModel
) {
    val email by sharedViewModel.email.collectAsStateWithLifecycle()
    val password by sharedViewModel.password.collectAsStateWithLifecycle()

    require(!email.isNullOrBlank()) {
        "Email missing in sharedViewModel"
    }

    val factory = remember {
        VerificationViewModelFactory(
            email = email!!,
            password = password,
            confirmSignUpUseCase = AppModule.confirmSignUpUseCase,
            autoLoginUseCase = AppModule.autoLoginUseCase,
            resendConfirmationCodeUseCase = AppModule.resendConfirmationCodeUseCase,
            startResetPasswordUseCase = AppModule.startResetPasswordUseCase
        )
    }

    val viewModel: VerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    val spacing = LocalSpacing.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Eventos
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when(event) {


                VerificationEvent.Success -> {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is VerificationEvent.NavigateToResetPassword -> {
                    val route = Routes.RESET_PASSWORD_WITH_ARG
                        .replace("{email}", event.email)
                    navController.navigate(route) {
                        popUpTo(Routes.VERIFICATION) { inclusive = true }
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
            text =  "Verifica tu correo"
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Ingresa el código que enviamos a tu correo para activar tu cuenta. Si no lo ves revisa tu carpeta de spam.",
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
