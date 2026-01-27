package com.akameiot.app.ui.navigation

//Esto dice: “voy a crear funciones que dibujan UI”.
import androidx.compose.runtime.Composable
//Importa NavHost, que es el contenedor principal de navegación.
import androidx.navigation.compose.NavHost
//Declara una ruta/pantalla dentro del NavHost.
import androidx.navigation.compose.composable
//función que crea y “recuerda” el controlador.
import androidx.navigation.compose.rememberNavController

//Importar las pantallas que se usaran en la navegacion de nuestra app
import com.akameiot.app.ui.home.HomeScreen
import com.akameiot.app.ui.login.LoginScreen
import com.akameiot.app.ui.login.RegisterScreen

@Composable
fun AppNavHost() {
    // Controlador que maneja la navegacion entre pantallas
    val navController = rememberNavController()

    // NavHost define que pantallas existen y cual es la inicial
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        //Aqui definimos nuestras rutas

        // Pantalla Login
        //composable es para crear una ruta en el nav
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Cuando el login sea exitoso, navegamos a Home
                    navController.navigate(Routes.HOME)
                }
            )
        }

        //Pantalla Register
        composable(Routes.REGISTER)
        {
            RegisterScreen()
        }
        // Pantalla Home
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
