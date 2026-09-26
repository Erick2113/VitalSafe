package com.example.vitalsafe.ui.state

data class HomeUiState(
    val currentLocation: String = "Buscando ubicación...",
    val emergencyContacts: List<String> = listOf("Elías", "Ana"),
    val isSosPressed: Boolean = false
)