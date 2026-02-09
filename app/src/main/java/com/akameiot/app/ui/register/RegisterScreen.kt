package com.akameiot.app.ui.register

import com.akameiot.app.ui.navigation.Routes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.components.AuthScaffold
import com.akameiot.coreui.components.TermsRow
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PasswordTextField
import com.akameiot.coreui.components.AuthHeader
import com.akameiot.coreui.theme.LocalSpacing
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akameiot.app.ui.navigation.VerificationType
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.flow.collectLatest



@Composable
fun RegisterScreen(
    navController: NavController,
) {

    val spacing = LocalSpacing.current
    val viewModel: RegisterViewModel = viewModel()

    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {

        viewModel.events.collectLatest { event ->

            when(event) {

                RegisterEvent.Success -> {

                    navController.navigate(
                        Routes.verification(VerificationType.REGISTER)
                    ) {
                        popUpTo(Routes.REGISTER) {
                            inclusive = true
                        }
                    }
                }

                is RegisterEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AuthScaffold (
            snackbarHostState = snackbarHostState
        ){

        AuthHeader(
            text = "Crea tu cuenta para continuar"
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

        Spacer(modifier = Modifier.height(spacing.md))

        PasswordTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            placeholder = "Confirmar contraseña",
            isError = !state.passwordsMatch
        )

        if (!state.passwordsMatch) {
            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        TermsRow(
            acceptedTerms = state.acceptedTerms,
            onCheckedChange = viewModel::onTermsAccepted,
            onTermsClick = {
                navController.navigate(Routes.TERMS)
            }
        )

        Spacer(modifier = Modifier.height(spacing.md))


        PrimaryButton(
            text = if(state.isLoading) "Creando cuenta..." else "Registrar cuenta",
            onClick = {

                viewModel.register()


            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isFormValid && !state.isLoading
        )
    }
}
