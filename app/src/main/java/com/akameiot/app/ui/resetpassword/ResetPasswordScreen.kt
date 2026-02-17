package com.akameiot.app.ui.resetpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.di.AppModule
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String
) {

    val spacing = LocalSpacing.current


    val factory = remember {
        ResetPasswordViewModelFactory(
            AppModule.passwordValidator,
            AppModule.confirmResetPasswordUseCase,
            email
        )
    }

    val viewModel: ResetPasswordViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {

        viewModel.events.collectLatest { event ->

            when(event) {

                ResetPasswordEvent.Success -> {

                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.RESET_PASSWORD_WITH_ARG) {
                            inclusive = true
                        }
                    }
                }

                is ResetPasswordEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AuthScaffold(
        snackbarHostState = snackbarHostState
    ) {

        AuthHeader(
            text = "Restablecer contraseña"
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Ingresa el código que enviamos a tu correo y define tu nueva contraseña.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        OtpField(
            value = state.code,
            onValueChange = viewModel::onCodeChange
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        PasswordTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Nueva contraseña",
            isError = state.password.isNotBlank() && !state.passwordValidation.isValid
        )

        if (state.password.isNotBlank() && !state.passwordValidation.isValid) {

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "La contraseña no cumple los requisitos",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        PasswordTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            placeholder = "Confirmar contraseña",
            isError = state.confirmPassword.isNotBlank() && !state.passwordsMatch
        )

        if (state.confirmPassword.isNotBlank() && !state.passwordsMatch) {

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = if (state.isLoading) "Cambiando..." else "Continuar",
            enabled = state.isFormValid && !state.isLoading,
            onClick = { viewModel.submit() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}