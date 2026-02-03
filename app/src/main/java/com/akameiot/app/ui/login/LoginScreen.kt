package com.akameiot.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akameiot.app.R
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PasswordTextField
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalSpacing


@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLogin: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

                Image(
                    painter = painterResource(id = R.drawable.akame_logo_text1),
                    contentDescription = "Akame Logo",
                    modifier = Modifier.size(220.dp)
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
                    onClick = { /* TODO recuperación */ },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Olvidé mi contraseña",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(spacing.md))

                PrimaryButton(
                    text = "Ingresar",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.md))


            }


        }
    }


