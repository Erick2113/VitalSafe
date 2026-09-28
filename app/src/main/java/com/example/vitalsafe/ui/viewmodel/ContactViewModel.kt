package com.example.vitalsafe.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vitalsafe.data.ContactPayload
import com.example.vitalsafe.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class ContactViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()
    private var contactsListener: ListenerRegistration? = null

    // Controla si vemos la lista o el formulario
    var showAddForm by mutableStateOf(false)

    // La lista de contactos, sincronizada en tiempo real con Firestore
    var contactList = mutableStateListOf<ContactPayload>()

    // Variables del formulario
    var contactName by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    var contactRelation by mutableStateOf("")
    var uiStatus by mutableStateOf("")
        private set
    var isSaving by mutableStateOf(false)
        private set
    var loadError by mutableStateOf("")
        private set

    init {
        auth.currentUser?.let { user ->
            contactsListener = repository.listenContacts(
                uid = user.uid,
                onChange = { contacts ->
                    loadError = ""
                    contactList.clear()
                    contactList.addAll(contacts)
                },
                onError = { loadError = "Error al cargar contactos: ${it.message}" }
            )
        }
    }

    fun clearForm() {
        contactName = ""
        contactPhone = ""
        contactRelation = ""
        uiStatus = ""
    }

    fun saveContact() {
        if (isSaving) return

        val user = auth.currentUser
        if (user == null) {
            uiStatus = "Error: No hay sesión activa."
            return
        }

        if (contactName.isBlank() || contactPhone.isBlank() || contactRelation.isBlank()) {
            uiStatus = "⚠️ Por favor, llena todos los campos."
            return
        }

        if (contactPhone.count { it.isDigit() } < 8) {
            uiStatus = "⚠️ Ingresa un número de teléfono válido."
            return
        }

        val payload = ContactPayload(
            ownerUid = user.uid,
            name = contactName.trim(),
            phone = contactPhone.trim(),
            relationship = contactRelation.trim(),
            timestamp = System.currentTimeMillis()
        )

        isSaving = true
        uiStatus = "Guardando contacto..."

        // La lista se actualiza sola gracias al listener de Firestore
        repository.addContact(
            uid = user.uid,
            contact = payload,
            onSuccess = {
                isSaving = false
                showAddForm = false
                clearForm()
            },
            onError = {
                isSaving = false
                uiStatus = "Error al guardar: ${it.message}"
            }
        )
    }

    override fun onCleared() {
        contactsListener?.remove()
        super.onCleared()
    }
}
