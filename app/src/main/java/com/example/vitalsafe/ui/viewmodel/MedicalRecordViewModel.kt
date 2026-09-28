package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MedicalRecordState(
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val medications: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
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
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(MedicalRecordState())
    val uiState: StateFlow<MedicalRecordState> = _uiState.asStateFlow()

    init {
        loadRecord()
    }

    // Trae lo que ya está guardado para editarlo
    private fun loadRecord() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No hay sesión activa.") }
            return
        }

        repository.getProfile(
            uid = user.uid,
            onResult = { profile ->
                _uiState.update {
                    it.copy(
                        bloodType = profile.bloodType,
                        allergies = profile.allergies,
                        conditions = profile.conditions,
                        medications = profile.medications,
                        isLoading = false
                    )
                }
            },
            onError = { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar: ${e.message}") }
            }
        )
    }

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

    fun saveRecord(onSaved: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(errorMessage = "No hay sesión activa.") }
            return
        }
        val state = _uiState.value
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        repository.saveProfileFields(
            uid = user.uid,
            fields = mapOf(
                "bloodType" to state.bloodType.trim().uppercase(),
                "allergies" to state.allergies.trim(),
                "conditions" to state.conditions.trim(),
                "medications" to state.medications.trim()
            ),
            onSuccess = {
                _uiState.update { it.copy(isSaving = false) }
                onSaved()
            },
            onError = { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar: ${e.message}") }
            }
        )
    }
}
