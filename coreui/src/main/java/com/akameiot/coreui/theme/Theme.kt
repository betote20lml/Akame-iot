package com.akameiot.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider


/* ─────────────────────────────
   🎨 PALETA DE COLORES
───────────────────────────── */

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E7D32), // verde fuerte
    onPrimary = Color.White,
    background = Color(0xFF101622),
    onBackground = Color.White,
    surface = Color(0xFF1A2032),
    onSurface = Color.White,
    error = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    background = Color(0xFFF6F6F8),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020)
)

/* ─────────────────────────────
   🧠 THEME PRINCIPAL
───────────────────────────── */

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    // Manejo correcto del sistema de ventanas
    SideEffect {
        val window = (view.context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


val LocalSpacing = staticCompositionLocalOf { Spacing() }