package com.example.vitalsafe.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.vitalsafe.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PersonalDataState(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class EditPersonalDataViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(PersonalDataState())
    val uiState: StateFlow<PersonalDataState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
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
                        fullName = profile.fullName,
                        phone = profile.phone,
                        address = profile.address,
                        // Si aún no guardó un correo, se sugiere el de su cuenta
                        email = profile.email.ifBlank { user.email.orEmpty() },
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
                "name" -> state.copy(fullName = value)
                "phone" -> state.copy(phone = value)
                "address" -> state.copy(address = value)
                "email" -> state.copy(email = value)
                else -> state
            }
        }
    }

    fun saveData(onSaved: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(errorMessage = "No hay sesión activa.") }
            return
        }
        val state = _uiState.value
        if (state.isSaving) return

        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "⚠️ El nombre es obligatorio.") }
            return
        }
        if (state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _uiState.update { it.copy(errorMessage = "⚠️ Ingresa un correo válido.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        repository.saveProfileFields(
            uid = user.uid,
            fields = mapOf(
                "fullName" to state.fullName.trim(),
                "phone" to state.phone.trim(),
                "address" to state.address.trim(),
                "email" to state.email.trim()
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
