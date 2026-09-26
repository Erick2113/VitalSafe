package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MedicalRecordState(
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val medications: String = ""
) {
    // Calcula el progreso basado en cuántos campos no están vacíos
    val progress: Float
        get() {
            var filled = 0f
            if (bloodType.isNotBlank()) filled += 1f
            if (allergies.isNotBlank()) filled += 1f
            if (conditions.isNotBlank()) filled += 1f
            if (medications.isNotBlank()) filled += 1f
            return filled / 4f // 4 es el número total de campos
        }
}

class MedicalRecordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MedicalRecordState())
    val uiState: StateFlow<MedicalRecordState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "blood" -> state.copy(bloodType = value)
                "allergies" -> state.copy(allergies = value)
                "conditions" -> state.copy(conditions = value)
                "medications" -> state.copy(medications = value)
                else -> state
            }
        }
    }
}