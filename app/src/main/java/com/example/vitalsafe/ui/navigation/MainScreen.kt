package com.example.vitalsafe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.dp

// Importaciones de Pantallas
import com.example.vitalsafe.ui.screens.usuario.HomeScreen
import com.example.vitalsafe.ui.screens.usuario.HistoryScreen
import com.example.vitalsafe.ui.screens.usuario.CompleteProfileScreen
import com.example.vitalsafe.ui.screens.usuario.EditMedicalRecordScreen
import com.example.vitalsafe.ui.screens.usuario.EditPersonalDataScreen
import com.example.vitalsafe.ui.screens.usuario.ContactScreen // Nueva pantalla

// Importaciones de ViewModels
import com.example.vitalsafe.ui.viewmodel.HomeViewModel
import com.example.vitalsafe.ui.viewmodel.HistoryViewModel
import com.example.vitalsafe.ui.viewmodel.ProfileViewModel
import com.example.vitalsafe.ui.viewmodel.EditPersonalDataViewModel
import com.example.vitalsafe.ui.viewmodel.ContactViewModel // Nuevo ViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { VitalSafeBottomBar(navController = navController) },
        containerColor = Color.White
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    viewModel = viewModel(),
                    onNavigateToProfile = { navController.navigate(BottomNavItem.Profile.route) }
                )
            }

            // Reemplazamos el texto temporal por la pantalla real
            composable(BottomNavItem.AddContact.route) {
                ContactScreen(
                    viewModel = viewModel()
                )
            }

            composable(BottomNavItem.History.route) {
                HistoryScreen(
                    viewModel = viewModel(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Profile.route) {
                CompleteProfileScreen(
                    viewModel = viewModel(),
                    onNavigateBack = { navController.popBackStack() },
                    onProfileSaved = { navController.navigate(BottomNavItem.Home.route) },
                    onEditMedicalRecord = { navController.navigate("edit_medical_record") },
                    onEditPersonalData = { navController.navigate("edit_personal_data") }
                )
            }
            composable("edit_medical_record") {
                EditMedicalRecordScreen(
                    viewModel = viewModel(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_personal_data") {
                EditPersonalDataScreen(
                    viewModel = viewModel(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun VitalSafeBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.AddContact,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF9800),
                    selectedTextColor = Color(0xFFFF9800),
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}