package com.akameiot.coreui.theme

import androidx.compose.runtime.collectAsState
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
import androidx.compose.runtime.getValue

data class AppColors(
    val cardBackground: Color,
    val cardBorder: Color,
    val staleBackground: Color,
    val staleBorder: Color,
    val divider: Color,
    val timestamp: Color,
    val metricValue: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}



private val DarkColorScheme = darkColorScheme(
    primary           = Color(0xFF4A9959),
    onPrimary         = Color.White,
    background        = Color(0xFF161E0E),
    onBackground      = Color.White,
    surface           = Color(0xFF1E2D1A),
    onSurface         = Color(0xFFE8EDE2),
    surfaceVariant    = Color(0xFF243320),
    onSurfaceVariant  = Color(0xFFB0B8A8),
    outline           = Color(0xFF3A4F34),
    outlineVariant    = Color(0xFF2A3D25),
    error             = Color(0xFFCF6679)
)


private val LightColorScheme = lightColorScheme(
    primary           = Color(0xFF4A9959),
    onPrimary         = Color.White,
    background        = Color(0xFFF4F6F2),
    onBackground      = Color(0xFF1A1C19),
    surface           = Color(0xFFFFFFFF),
    onSurface         = Color(0xFF1A1C19),
    surfaceVariant    = Color(0xFFEEF2E8),
    onSurfaceVariant  = Color(0xFF5C6358),
    outline           = Color(0xFFC4CAC0),
    outlineVariant    = Color(0xFFDDE3D5),
    error             = Color(0xFFB00020)
)

private val LightAppColors = AppColors(
    cardBackground = Color.White,
    cardBorder = Color(0xFFDDE3D5),

    staleBackground = Color(0xFFFFFDE7),
    staleBorder = Color(0xFFD4BC6A),

    divider = Color(0xFFE8EDE2),
    timestamp = Color(0xFF9AA09A),
    metricValue = Color(0xFF2E7D32)
)

private val DarkAppColors = AppColors(
    cardBackground = Color(0xFF1E2D1A),
    cardBorder = Color(0xFF2E4228),

    staleBackground = Color(0xFF2A2510),
    staleBorder = Color(0xFFC4A84A),

    divider = Color(0xFF2A3D25),
    timestamp = Color(0xFF6B7B65),
    metricValue = Color(0xFF66BB6A)
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val isDark by ThemeController.isDark.collectAsState()
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val appColors   = if (isDark) DarkAppColors else LightAppColors
    val view = LocalView.current

    // Manejo correcto del sistema de ventanas
    SideEffect {
        val window = (view.context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
                LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


val LocalSpacing = staticCompositionLocalOf { Spacing() }