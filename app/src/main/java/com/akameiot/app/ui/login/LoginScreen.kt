package com.akameiot.app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PasswordTextField
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalSpacing
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.app.ui.navigation.VerificationType
import com.akameiot.app.ui.navigation.Routes
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    navController: NavController,
    onLogin: () -> Unit = {},
) {

    val spacing = LocalSpacing.current
    val viewModel: LoginViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    AuthScaffold {

        AuthHeader(
            text = "Bienvenido de vuelta"
        )

        Spacer(modifier = Modifier.height(spacing.md))

        AppTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo electrónico",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(spacing.md))

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña"
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        TextButton(
            onClick = {
                navController.navigate(
                    Routes.verification(VerificationType.PASSWORDLESS_LOGIN)
                )
            },
            modifier = Modifier.align(Alignment.End),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Olvidé mi contraseña",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        PrimaryButton(
            text = "Ingresar",
            onClick = {
                viewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        )

    }
}

