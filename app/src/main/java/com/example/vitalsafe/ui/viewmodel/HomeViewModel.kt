package com.example.vitalsafe.ui.viewmodel

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

// El modelo de datos exacto que Adrián mandará a la base de datos
data class EmergencyPayload(
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L
)

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Guardamos la ubicación en tiempo real aquí
    var currentLocation by mutableStateOf<Location?>(null)
        private set

    // Para mostrarle al usuario qué está pasando
    var sosStatus by mutableStateOf("Sistema listo")
        private set

    fun updateLocation(location: Location?) {
        currentLocation = location
    }

    fun triggerSOS() {
        val user = auth.currentUser
        val location = currentLocation

        if (user == null) {
            sosStatus = "Error: Inicia sesión primero"
            return
        }
        if (location == null) {
            sosStatus = "Error: Calculando GPS, intenta de nuevo..."
            return
        }

        // Empaquetamos los datos dinámicos
        val payload = EmergencyPayload(
            uid = user.uid,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis()
        )

        sosStatus = "¡Alerta enviando coordenadas!"

        // ==========================================
        // ---> ÁREA DE ADRIÁN <---
        // ==========================================
        enviarAFirebase(payload)
    }

    private fun enviarAFirebase(payload: EmergencyPayload) {
        // ADRIÁN: Aquí haces la instancia de FirebaseFirestore
        // y haces el .add(payload) a tu colección de emergencias.
    }
}