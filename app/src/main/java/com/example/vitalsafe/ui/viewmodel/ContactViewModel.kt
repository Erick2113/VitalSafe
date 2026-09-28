package com.example.vitalsafe.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

data class ContactPayload(
    val ownerUid: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val timestamp: Long = 0L
)

class ContactViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Controla si vemos la lista o el formulario
    var showAddForm by mutableStateOf(false)

    // La lista de contactos (Adrián la llenará desde la base de datos)
    var contactList = mutableStateListOf<ContactPayload>()

    // Variables del formulario
    var contactName by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    var contactRelation by mutableStateOf("")
    var uiStatus by mutableStateOf("")
        private set

    fun clearForm() {
        contactName = ""
        contactPhone = ""
        contactRelation = ""
        uiStatus = ""
    }

    fun saveContact() {
        val user = auth.currentUser
        if (user == null) {
            uiStatus = "Error: No hay sesión activa."
            return
        }

        if (contactName.isBlank() || contactPhone.isBlank() || contactRelation.isBlank()) {
            uiStatus = "⚠️ Por favor, llena todos los campos."
            return
        }

        val payload = ContactPayload(
            ownerUid = user.uid,
            name = contactName.trim(),
            phone = contactPhone.trim(),
            relationship = contactRelation.trim(),
            timestamp = System.currentTimeMillis()
        )

        uiStatus = "Guardando contacto..."

        // ==========================================
        // ---> ÁREA DE ADRIÁN <---
        // ==========================================
        // Aquí Adrián sube 'payload' a Firebase.
        // Al terminar con éxito, debe ejecutar esto:
        // showAddForm = false
        // clearForm()
        // Y añadir el contacto a 'contactList' para que aparezca en pantalla.
    }
}