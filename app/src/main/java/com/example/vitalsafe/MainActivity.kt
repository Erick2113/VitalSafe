package com.example.vitalsafe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vitalsafe.ui.screens.RegisterScreen
import com.example.vitalsafe.location.LocationTrackingService
import com.example.vitalsafe.ui.navigation.MainScreen
import com.example.vitalsafe.ui.screens.LoginScreen
import com.example.vitalsafe.ui.theme.VitalSafeTheme
import com.example.vitalsafe.ui.viewmodel.AuthViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitalSafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val rootNavController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()

                    val startDestination = remember {
                        if (authViewModel.isLoggedIn) "main_screen" else "login"
                    }

                    NavHost(navController = rootNavController, startDestination = startDestination) {

                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    rootNavController.navigate("main_screen") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                // <-- AQUÍ CONECTAMOS EL PUENTE HACIA EL REGISTRO
                                onNavigateToRegister = {
                                    rootNavController.navigate("register")
                                }
                            )
                        }

                        // <-- NUEVA RUTA: Aquí cargaremos el escáner del DUI
                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateBack = { rootNavController.popBackStack() },
                                onRegisterSuccess = {
                                    rootNavController.navigate("main_screen") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main_screen") {
                            MainScreen(
                                onLogout = {
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