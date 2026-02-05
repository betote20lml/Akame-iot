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


@Composable
fun RegisterScreen(
    navController: NavController,
    onRegister: () -> Unit = {},
) {

    val spacing = LocalSpacing.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    val passwordsMatch =
        confirmPassword.isEmpty() || password == confirmPassword

    AuthScaffold {

        AuthHeader(
            text = "Crea tu cuenta para continuar"
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

        Spacer(modifier = Modifier.height(spacing.md))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
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
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = acceptedTerms
        )

        Spacer(modifier = Modifier.height(spacing.md))

        TermsRow(
            acceptedTerms = acceptedTerms,
            onCheckedChange = { acceptedTerms = it },
            onTermsClick = {
                navController.navigate(Routes.TERMS)
            }
        )
    }
}
