package com.akameiot.app

//Bundle es un objeto que Android usa para guardar/recuperar datos del estado.
import android.os.Bundle
//Activity = una pantalla base de Android (la ventana principal).
import androidx.activity.ComponentActivity
//setContent { ... } es la función que conecta Compose con tu Activity.
import androidx.activity.compose.setContent
//Importa tu función AppNavHost(). Esto es tu “router”/navegación:
import com.akameiot.app.ui.navigation.AppNavHost
//En web sería como tu “CSS global” o tu tema .
import com.akameiot.app.ui.theme.AkameIotTheme


//Imports para abarcar la pantalla completa
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import android.graphics.Color as AndroidColor
import android.os.Build
import android.view.WindowManager


//Crear clase con herencia del ComponentActivity
//MainActivity monta el escenario, AppNavHost dirige la obra.
class MainActivity : ComponentActivity() {
    //onCreat se ejecuta cuando Android crea esta pantalla por primera vez
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

// Permitir dibujar en el area del notch/cutout (si el telefono lo tiene)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

// Barras transparentes
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            AkameIotTheme {
                AppNavHost()
            }
        }
    }

}