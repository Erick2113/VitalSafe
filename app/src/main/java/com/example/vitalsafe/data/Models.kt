package com.example.vitalsafe.data

// Datos del documento users/{uid}
data class UserProfile(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val medications: String = ""
)

// Documento de users/{uid}/contacts
data class ContactPayload(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val timestamp: Long = 0L
)

// Documento de users/{uid}/emergencies
data class EmergencyPayload(
    val id: String = "",
    val uid: String = "",
    val type: String = "Alerta SOS",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val status: String = "Enviada",
    val timestamp: Long = 0L
)
