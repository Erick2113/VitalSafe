package com.example.vitalsafe.ui.state

data class ProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "", // Padecimientos
    val currentMedications: String = "", // Medicamentos actuales
    val emergencyContact: String = "", // Primer contacto registrado
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    // Iniciales para el avatar: "Juan Rodríguez" -> "JR"
    val initials: String
        get() = fullName.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}
