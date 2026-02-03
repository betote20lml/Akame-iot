package com.akameiot.app.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akameiot.app.R
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.components.SecondaryButton
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PasswordTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.HorizontalDivider
import com.akameiot.coreui.theme.LocalSpacing


@Composable
fun RegisterScreen(
    onRegister: () -> Unit = {},
    onGuestLogin: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val spacing = LocalSpacing.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordsMatch =
        confirmPassword.isEmpty() || password == confirmPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(spacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            /* ───────────── CONTENIDO ───────────── */

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.akame_logo_text1),
                    contentDescription = "Akame Logo",
                    modifier = Modifier.size(220.dp)
                )




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
                )

                Spacer(modifier = Modifier.height(spacing.md))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))

                    Text(
                        text = "o",
                        modifier = Modifier.padding(horizontal = spacing.md),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(spacing.md))


                SecondaryButton(
                    text = "Entrar como invitado",
                    onClick = onGuestLogin,
                    icon = Icons.Default.QrCodeScanner
                )
            }

            Spacer(modifier = Modifier.height(spacing.sm))
            /* ───────────── FOOTER ───────────── */

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .wrapContentWidth()
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(spacing.xs))


                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Inicia sesión",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

