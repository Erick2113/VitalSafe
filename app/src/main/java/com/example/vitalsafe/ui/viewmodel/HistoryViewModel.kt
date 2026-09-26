package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.ui.state.EmergencyItem
import com.example.vitalsafe.ui.state.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {
    // Cargamos los datos del Figma por defecto al inicializar
    private val _uiState = MutableStateFlow(
        HistoryUiState(
            emergencies = listOf(
                EmergencyItem("Dificultad...", "12 Oct 2023, 10:30 AM", "Colonia Escalón, San Salvador"),
                EmergencyItem("Accidente de...", "08 Sep 2023, 08:15 AM", "Bulevar Los Próceres, San..."),
                EmergencyItem("Caída Leve en Vi...", "20 Ago 2023, 14:00 PM", "Avenida Jerusalén, San..."),
                EmergencyItem("Intoxicación...", "05 Jul 2023, 19:45 PM", "Antiguo Cuscatlán, La Libertad")
            )
        )
    )
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
}