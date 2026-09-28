package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.data.EmergencyPayload
import com.example.vitalsafe.data.UserRepository
import com.example.vitalsafe.ui.state.EmergencyItem
import com.example.vitalsafe.ui.state.HistoryUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()
    private var emergenciesListener: ListenerRegistration? = null

    // Lista completa sin filtrar; la UI muestra solo lo que coincide con la búsqueda
    private var allEmergencies: List<EmergencyItem> = emptyList()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.forLanguageTag("es-SV"))

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No hay sesión activa.") }
        } else {
            emergenciesListener = repository.listenEmergencies(
                uid = user.uid,
                onChange = { emergencies ->
                    allEmergencies = emergencies.map { it.toItem() }
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    applyFilter()
                },
                onError = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar historial: ${e.message}") }
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.trim()
        val filtered = if (query.isEmpty()) {
            allEmergencies
        } else {
            allEmergencies.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        item.date.contains(query, ignoreCase = true) ||
                        item.location.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(emergencies = filtered) }
    }

    private fun EmergencyPayload.toItem() = EmergencyItem(
        title = type.ifBlank { "Alerta SOS" },
        date = dateFormat.format(Date(timestamp)),
        location = address.ifBlank { "%.5f, %.5f".format(Locale.US, latitude, longitude) },
        status = status.ifBlank { "Enviada" }
    )

    override fun onCleared() {
        emergenciesListener?.remove()
        super.onCleared()
    }
}
