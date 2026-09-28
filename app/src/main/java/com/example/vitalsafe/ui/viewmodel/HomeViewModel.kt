package com.example.vitalsafe.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitalsafe.data.EmergencyPayload
import com.example.vitalsafe.data.UserRepository
import com.example.vitalsafe.location.LocationTracker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    // Ubicación en tiempo real que alimenta el servicio de rastreo
    val currentLocation: StateFlow<Location?> = LocationTracker.location

    // Para mostrarle al usuario qué está pasando
    var sosStatus by mutableStateOf("Sistema listo")
        private set

    var isSending by mutableStateOf(false)
        private set

    fun updateLocation(location: Location) {
        LocationTracker.update(location)
    }

    fun triggerSOS() {
        if (isSending) return

        val user = auth.currentUser
        if (user == null) {
            sosStatus = "Error: Inicia sesión primero"
            return
        }

        // Si la ubicación rastreada es reciente se usa al instante; si no, se pide una nueva
        val known = LocationTracker.location.value
        if (known != null && System.currentTimeMillis() - known.time < MAX_LOCATION_AGE_MS) {
            sendEmergency(user.uid, known)
        } else {
            requestFreshLocation(user.uid)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(uid: String) {
        if (!LocationTracker.hasLocationPermission(getApplication())) {
            sosStatus = "Error: Se requiere permiso de ubicación"
            return
        }

        isSending = true
        sosStatus = "Obteniendo tu ubicación..."

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                // Si el GPS no responde, se usa la última ubicación rastreada aunque sea antigua
                val fallback = location ?: LocationTracker.location.value
                if (fallback != null) {
                    if (location != null) LocationTracker.update(location)
                    sendEmergency(uid, fallback)
                } else {
                    isSending = false
                    sosStatus = "Error: Calculando GPS, intenta de nuevo..."
                }
            }
            .addOnFailureListener {
                isSending = false
                sosStatus = "Error al obtener ubicación: ${it.message}"
            }
    }

    private fun sendEmergency(uid: String, location: Location) {
        isSending = true
        sosStatus = "¡Alerta enviando coordenadas!"

        viewModelScope.launch {
            val address = withContext(Dispatchers.IO) { resolveAddress(location) }

            val payload = EmergencyPayload(
                uid = uid,
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                timestamp = System.currentTimeMillis()
            )

            repository.addEmergency(
                uid = uid,
                emergency = payload,
                onSuccess = {
                    isSending = false
                    sosStatus = "✅ Alerta enviada: $address"
                },
                onError = {
                    isSending = false
                    sosStatus = "Error al enviar la alerta: ${it.message}"
                }
            )
            repository.updateLastLocation(uid, location.latitude, location.longitude)
        }
    }

    // Convierte coordenadas en una dirección legible; si falla, deja las coordenadas
    private fun resolveAddress(location: Location): String {
        val coordinates = String.format(Locale.US, "%.5f, %.5f", location.latitude, location.longitude)
        if (!Geocoder.isPresent()) return coordinates

        return try {
            @Suppress("DEPRECATION")
            Geocoder(getApplication(), Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
                ?: coordinates
        } catch (e: Exception) {
            coordinates
        }
    }

    companion object {
        private const val MAX_LOCATION_AGE_MS = 2 * 60 * 1000L
    }
}
