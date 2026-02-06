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




@Composable
fun RegisterScreen(
    navController: NavController,
    onRegister: () -> Unit = {},
) {

    val spacing = LocalSpacing.current
    val viewModel: RegisterViewModel = viewModel()


    val state by viewModel.uiState.collectAsState()

    val passwordsMatch =
        state.confirmPassword.isEmpty() || state.password == state.confirmPassword

    AuthScaffold {

        AuthHeader(
            text = "Crea tu cuenta para continuar"
        )

        Spacer(modifier = Modifier.height(spacing.md))

        AppTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Correo electrónico",
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
            isError = !passwordsMatch
        )

        if (!passwordsMatch) {
            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        PrimaryButton(
            text = "Registrar cuenta",
            onClick = {

                viewModel.register()

                navController.navigate(
                    Routes.verification(VerificationType.REGISTER)
                ) {
                    popUpTo(Routes.REGISTER) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.acceptedTerms
        )

        Spacer(modifier = Modifier.height(spacing.md))

        TermsRow(
            acceptedTerms = state.acceptedTerms,
            onCheckedChange = viewModel::onTermsAccepted,
            onTermsClick = {
                navController.navigate(Routes.TERMS)
            }
        )
    }
}
