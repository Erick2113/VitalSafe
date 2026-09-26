package com.example.vitalsafe.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add // Usaremos el ícono de + estándar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Inicio", Icons.Default.Home)
    object AddContact : BottomNavItem("add_contact", "Añadir contacto", Icons.Default.Add) // Ícono corregido
    object History : BottomNavItem("history", "Historial", Icons.Default.Refresh)
    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}