package com.akameiot.app.ui.login

// Imports: traen componentes y utilidades que usaremos en la UI
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button

//Importar colores de los temas
import androidx.compose.material3.MaterialTheme

//Agregar la paqueteria de iconos, pero primero agregamos la dependencia en el build.gradle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

//Para que corra cosas a tiempo real
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

//Manejo de focus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//Para poder agregar imagenes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.akameiot.app.R



// @Composable indica que esta funcion dibuja interfaz de usuario
@Composable
fun LoginScreen(
    // Funcion que se ejecutara cuando el login sea exitoso
    // No recibe parametros y no regresa nada (Unit = void)
    onLoginSuccess: () -> Unit
) {
    // Estado local (solo UI). Mas adelante esto lo movemos a ViewModel.
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberUser by remember { mutableStateOf(false) }

    //Estados para la validacion de campos String? permite que el valor sea null
    var userError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }

    //Crea el FocusRequester para el password
    val userFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    var passwordVisible by remember {mutableStateOf(false)}
    val keyboardController = LocalSoftwareKeyboardController.current

    // Colores desde el Theme (ya no hardcodeos)
    val bgColor = MaterialTheme.colorScheme.background
    val panelColor = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    //Colores para los Inputs
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedContainerColor = panelColor, // A: gris cuando no hay foco
        errorContainerColor = MaterialTheme.colorScheme.onPrimary,// blanco evita “flash” en error
        focusedBorderColor = primary,
        unfocusedBorderColor = Color.Gray,
        errorBorderColor = errorColor,
        focusedLabelColor = primary,
        cursorColor = primary
    )

    //Funciones
    //Validar el login
    fun validateAndLogin() {
        val u = user.trim()

        //Verificar si hay datos en las variables del usuario
        userError = if (u.isEmpty()) "El usuario es obligatorio" else null
        passError = when {
            password.isEmpty() -> "La contraseña es obligatoria"
            password.length < 6 -> "Minimo 6 caracteres"
            else -> null
        }

        when {
            userError != null -> userFocusRequester.requestFocus()
            passError != null -> passwordFocusRequester.requestFocus()
            else -> {
                keyboardController?.hide()
                onLoginSuccess()
            }
        }
    }

    // Box es un contenedor, como un div en HTML
    Box(
        // Modifier sirve para aplicar "estilos" (tamano, padding, etc.)
        // fillMaxSize() = ocupa toda la pantalla
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        // Alinea el contenido del Box al centro
        contentAlignment = Alignment.Center
    ) {
        // Panel principal (tarjeta redondeada)
        Column(
            modifier = Modifier
                .width(300.dp) // ajusta si quieres mas ancho
                .background(
                    color = panelColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Input Usuario
            OutlinedTextField(
                value = user,
                onValueChange = {
                    user = it
                    if (userError != null) userError = null //Limpia error al ecribir
                },
                label = { Text(text = "Usuario")},
                singleLine = true,
                isError = userError != null,
                supportingText = { if (userError != null) Text(userError!!)},
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(userFocusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Input Contrasena
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passError != null) passError = null
                },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = passError != null,
                supportingText = { if (passError != null) Text(passError!!)},
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                    else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                //Ponemos los Iconos que vienen en el package trailingIcon (parte derecha del campo)
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Ocultar contrasena"
                            else
                                "Mostrar contrasena"
                        )
                    }
                },
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Checkbox + texto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rememberUser = !rememberUser},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberUser,
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recordar mi usuario")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Area de LOGO (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.akame_logo_text1),
                    contentDescription = "Akame IoT Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Boton temporal para probar flujo (despues lo hacemos login real)
            Button(
                onClick = { validateAndLogin() },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(height = 15.dp))

            //Agregar nuevo usuario
            Text(
                text = "Registrar nuevo usuario",
                style = TextStyle(
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline
                    ),
                color = primary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable {
                        // aqui luego navegas a RegisterScreen
                        println("Click en registrar nuevo usuario")
                    }
            )

        }
    }
}