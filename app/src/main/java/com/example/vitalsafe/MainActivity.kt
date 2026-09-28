package com.example.vitalsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Importaciones de tus pantallas y ViewModel
import com.example.vitalsafe.location.LocationTrackingService
import com.example.vitalsafe.ui.navigation.MainScreen
import com.example.vitalsafe.ui.screens.LoginScreen
import com.example.vitalsafe.ui.theme.VitalSafeTheme
import com.example.vitalsafe.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitalSafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Creamos un controlador de navegación maestro solo para el Login vs App
                    val rootNavController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()

                    // Si ya había una sesión abierta, se entra directo a la app
                    val startDestination = remember {
                        if (authViewModel.isLoggedIn) "main_screen" else "login"
                    }

                    NavHost(navController = rootNavController, startDestination = startDestination) {

                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    // Viajamos a la app principal
                                    rootNavController.navigate("main_screen") {
                                        // Truco pro: Destruimos la pantalla de login del historial
                                        // para que si el usuario presiona "Atrás", se salga de la app
                                        // en vez de volver a ver el formulario de login.
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main_screen") {
                            // Aquí cargamos toda la barra de navegación y pantallas que ya hicimos
                            MainScreen(
                                onLogout = {
                                    // Se detiene el rastreo antes de cerrar la sesión
                                    LocationTrackingService.stop(this@MainActivity)
                                    authViewModel.logout()
                                    rootNavController.navigate("login") {
                                        popUpTo("main_screen") { inclusive = true }
                                    }
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}