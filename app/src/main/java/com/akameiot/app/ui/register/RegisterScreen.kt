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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import com.akameiot.app.ui.auth.AuthSharedViewModel
import com.akameiot.di.AppModule
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    navController: NavController,
    sharedViewModel: AuthSharedViewModel
) {

    val spacing = LocalSpacing.current
    val factory = remember {
        RegisterViewModelFactory(
            AppModule.registerUseCase,
            AppModule.passwordValidator
        )
    }
    val viewModel: RegisterViewModel = viewModel(
        factory = factory
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {

        viewModel.events.collectLatest { event ->

            when (event) {
                is RegisterEvent.Success -> {

                    sharedViewModel.setCredentials(
                        email = state.email,
                        password = state.password
                    )

                    navController.navigate(Routes.VERIFICATION) {
                        launchSingleTop = true
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
            placeholder = "Contraseña",
            isError = state.password.isNotBlank() && !state.passwordValidation.isValid
        )

        val validation = state.passwordValidation


        if (state.password.isNotBlank() && !state.passwordValidation.isValid) {

                    Spacer(modifier = Modifier.height(spacing.xs))

                    val errorMessage = when {
                        !validation.hasMinLength ->
                            "Debe tener al menos 8 caracteres"
                        !validation.hasLowercase ->
                            "Debe contener al menos una letra minúscula"
                        !validation.hasNumber ->
                            "Debe contener al menos un número"
                        else -> ""
                    }

                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
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
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        TermsRow(
            acceptedTerms = state.acceptedTerms,
            onCheckedChange = { if(!state.isLoading) viewModel.onTermsAccepted(it) },
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
