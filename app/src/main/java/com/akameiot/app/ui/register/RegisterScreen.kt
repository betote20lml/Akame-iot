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
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PasswordTextField
import com.akameiot.coreui.theme.LocalSpacing
import androidx.navigation.NavController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink


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

    AuthScaffold(

        bottomContent = {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it }
                )

                val annotatedString = buildAnnotatedString {

                    append("Acepto los ")

                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "terms",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ),
                            linkInteractionListener = {
                                navController.navigate(Routes.TERMS)
                            }
                        )
                    ) {
                        append("Términos y Condiciones")
                    }
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }

    ) {

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
    }
}
