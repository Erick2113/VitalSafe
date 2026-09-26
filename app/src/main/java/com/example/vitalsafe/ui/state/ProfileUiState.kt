package com.example.vitalsafe.ui.state

data class ProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "", // Padecimientos
    val currentMedications: String = "" // Medicamentos actuales[cite: 7]
)