package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.ui.state.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateFullName(name: String) = _uiState.update { it.copy(fullName = name) }
    fun updatePhone(phone: String) = _uiState.update { it.copy(phone = phone) }
    fun updateBloodType(type: String) = _uiState.update { it.copy(bloodType = type) }
    fun updateAllergies(allergies: String) = _uiState.update { it.copy(allergies = allergies) }
    fun updateConditions(conditions: String) = _uiState.update { it.copy(conditions = conditions) }
    fun updateMedications(meds: String) = _uiState.update { it.copy(currentMedications = meds) }

    fun saveProfile() {
        // A futuro: lógica para guardar en Firestore
    }
}