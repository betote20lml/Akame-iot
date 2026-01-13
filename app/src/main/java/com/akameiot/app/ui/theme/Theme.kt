package com.akameiot.app.ui.theme

//Función que detecta si el sistema está en modo oscuro
import androidx.compose.foundation.isSystemInDarkTheme
//MaterialTheme = el contenedor global del tema.
import androidx.compose.material3.MaterialTheme
//darkColorScheme y lightColorScheme crean esquemas de color
// “con roles” para Material 3.
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
//Para poder crear la función AkameIotTheme como Composable
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    //primary es el color principal de la app.
    primary = AkameBlue,
    //secondary es un color de apoyo.
    secondary = AkameBlueDark,
    //background: fondo general.
    background = AkameBackground,
    //surface: tarjetas/paneles/superficies.
    surface = AkameSurface,
    //Aquí defines que “error” es rojo.
    error = AkameError,

    //onX = color del contenido encima de X
    //si el botón es primary (azul), el texto debe ser onPrimary (blanco).
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = Black,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = AkameBlue,
    secondary = AkameBlueDark,
    background = AkameBackground,
    surface = AkameSurface,
    error = AkameError,

    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = Black,
    onError = White
)

@Composable
fun AkameIotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
