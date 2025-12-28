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

//Crear clase con herencia del ComponentActivity
//MainActivity monta el escenario, AppNavHost dirige la obra.
class MainActivity : ComponentActivity() {
    //onCreat se ejecuta cuando Android crea esta pantalla por primera vez
    override fun onCreate(savedInstanceState: Bundle?) {
    //override Estoy reemplazando el metodo onCreate del padre (ComponentActivity).

        //Android necesita inicializar cosas internas
        //Si no lo llamas, pueden fallar cosas
        //📌 Regla general: siempre llamar a super en onCreate.
        super.onCreate(savedInstanceState)

        //Aquí empieza la UI Compose.
        //
        //Todo lo que pongas dentro de { } se dibuja en pantalla.
        setContent {
            AkameIotTheme {
                AppNavHost()
            }
        }
    }
}