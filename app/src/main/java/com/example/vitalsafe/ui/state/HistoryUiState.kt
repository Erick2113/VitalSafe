package com.example.vitalsafe.ui.state

// Modelo de datos para un solo elemento de la lista basado en el Figma
data class EmergencyItem(
    val title: String,
    val date: String,
    val location: String,
    val status: String = "Completado" // Estado por defecto en tu diseño
)

data class HistoryUiState(
    val emergencies: List<EmergencyItem> = emptyList(),
    val isLoading: Boolean = false
)