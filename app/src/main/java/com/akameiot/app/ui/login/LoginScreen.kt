package com.akameiot.app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import com.akameiot.app.ui.navigation.VerificationType
import com.akameiot.app.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun LoginScreen(
    navController: NavController,
) {

    val spacing = LocalSpacing.current
    val viewModel: LoginViewModel = viewModel()

    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {

        viewModel.events.collectLatest { event ->

            when(event) {

                LoginEvent.Success -> {

                    navController.navigate(Routes.LANDING) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }

                LoginEvent.NavigateToPasswordRecovery -> {

                    navController.navigate(
                        Routes.verification(VerificationType.PASSWORDLESS_LOGIN)
                    )
                }

                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AuthScaffold(
        snackbarHostState = snackbarHostState
    ) {

        AuthHeader(
            text = "Bienvenido de vuelta"
        )

        Spacer(modifier = Modifier.height(spacing.md))

        AppTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(spacing.md))

        PasswordTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Contraseña"
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        TextButton(
            onClick = {
                viewModel.onForgotPasswordClick()
            },
            modifier = Modifier.align(Alignment.End),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Olvidé mi contraseña")
        }

        Spacer(modifier = Modifier.height(spacing.md))

        PrimaryButton(
            text = if(state.isLoading) "Ingresando..." else "Ingresar",
            onClick = {
                viewModel.login()
            },
            enabled = state.isFormValid && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

