package com.example.vitalsafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
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

    // Se regresa a Idle para que el login no navegue solo al volver a mostrarse
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}
