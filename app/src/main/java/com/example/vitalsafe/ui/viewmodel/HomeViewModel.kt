package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun triggerSos() {
        _uiState.update { it.copy(isSosPressed = true) }
    }

    fun updateLocation(newLocation: String) {
        _uiState.update { it.copy(currentLocation = newLocation) }
    }
}