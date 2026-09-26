package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PersonalDataState(
    val fullName: String = "Juan Rodriguez",
    val phone: String = "6458 9158",
    val address: String = "Col. San Rafael casa 25",
    val email: String = "Juan@gmail.com"
)

class EditPersonalDataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalDataState())
    val uiState: StateFlow<PersonalDataState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "name" -> state.copy(fullName = value)
                "phone" -> state.copy(phone = value)
                "address" -> state.copy(address = value)
                "email" -> state.copy(email = value)
                else -> state
            }
        }
    }
}