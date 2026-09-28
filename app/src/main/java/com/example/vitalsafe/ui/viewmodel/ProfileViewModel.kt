package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vitalsafe.data.UserRepository
import com.example.vitalsafe.ui.state.ProfileUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Solo lectura: el perfil se actualiza en tiempo real cuando se editan los datos o el expediente
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()
    private val listeners = mutableListOf<ListenerRegistration>()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No hay sesión activa.") }
        } else {
            listeners += repository.listenProfile(
                uid = user.uid,
                onChange = { profile ->
                    _uiState.update {
                        it.copy(
                            fullName = profile.fullName,
                            phone = profile.phone,
                            address = profile.address,
                            email = profile.email.ifBlank { user.email.orEmpty() },
                            bloodType = profile.bloodType,
                            allergies = profile.allergies,
                            conditions = profile.conditions,
                            currentMedications = profile.medications,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onError = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar perfil: ${e.message}") }
                }
            )

            listeners += repository.listenContacts(
                uid = user.uid,
                onChange = { contacts ->
                    val first = contacts.firstOrNull()
                    _uiState.update {
                        it.copy(emergencyContact = first?.let { c -> "${c.name} - ${c.phone}" }.orEmpty())
                    }
                },
                onError = { /* El error principal ya se muestra desde el perfil */ }
            )
        }
    }

    override fun onCleared() {
        listeners.forEach { it.remove() }
        super.onCleared()
    }
}
