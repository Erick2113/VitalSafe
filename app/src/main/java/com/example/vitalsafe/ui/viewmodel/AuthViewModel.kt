package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // <-- IMPORTACIÓN PARA LA BASE DE DATOS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // <-- CONEXIÓN A FIRESTORE

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // Mantiene la sesión abierta si el usuario ya se había logueado antes
    fun checkCurrentUser() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Llena todos los campos")
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error al iniciar sesión")
                }
            }
    }

    // <-- NUEVA FUNCIÓN DE REGISTRO CON CREACIÓN DE EXPEDIENTE -->
    fun register(email: String, pass: String, fullName: String, dui: String, phone: String) {
        if (email.isBlank() || pass.isBlank() || fullName.isBlank() || dui.isBlank()) {
            _authState.value = AuthState.Error("Llena todos los campos requeridos")
            return
        }

        _authState.value = AuthState.Loading

        // 1. Creamos la cuenta de acceso en Firebase Auth
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // 2. Preparamos el expediente médico inicial en Firestore
                        val userProfile = hashMapOf(
                            "uid" to user.uid,
                            "fullName" to fullName.trim(),
                            "email" to email.trim(),
                            "phone" to phone.trim(),
                            "dui" to dui.trim(),
                            "role" to "ciudadano", // <-- Se asigna automáticamente el rol
                            "address" to "",
                            "bloodType" to "",
                            "allergies" to "",
                            "conditions" to "",
                            "medications" to ""
                        )

                        // 3. Guardamos el documento usando el ID único (UID) del usuario como llave
                        db.collection("users").document(user.uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Error al guardar el perfil en la nube")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error al registrar la cuenta")
                }
            }
    }

    // Se regresa a Idle para que el login no navegue solo al volver a mostrarse
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}