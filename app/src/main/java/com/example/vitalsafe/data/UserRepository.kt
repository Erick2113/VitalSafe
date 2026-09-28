package com.example.vitalsafe.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

/**
 * Único punto de acceso a Firestore. Estructura:
 *   users/{uid}                  -> perfil, expediente médico y lastLocation
 *   users/{uid}/contacts/{id}    -> contactos de emergencia
 *   users/{uid}/emergencies/{id} -> alertas SOS enviadas
 */
class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun userDoc(uid: String) = db.collection(USERS).document(uid)
    private fun contacts(uid: String) = userDoc(uid).collection(CONTACTS)
    private fun emergencies(uid: String) = userDoc(uid).collection(EMERGENCIES)

    // ---------- Perfil / expediente ----------

    fun getProfile(
        uid: String,
        onResult: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ) {
        userDoc(uid).get()
            .addOnSuccessListener { onResult(it.toUserProfile()) }
            .addOnFailureListener { onError(it) }
    }

    fun listenProfile(
        uid: String,
        onChange: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        userDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null) onError(error)
            else onChange(snapshot?.toUserProfile() ?: UserProfile())
        }

    // merge: solo sobrescribe los campos enviados, no borra el resto del documento
    fun saveProfileFields(
        uid: String,
        fields: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        userDoc(uid).set(fields + ("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updateLastLocation(uid: String, latitude: Double, longitude: Double) {
        val lastLocation = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDoc(uid).set(mapOf("lastLocation" to lastLocation), SetOptions.merge())
    }

    // ---------- Contactos ----------

    fun addContact(
        uid: String,
        contact: ContactPayload,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = mapOf(
            "ownerUid" to contact.ownerUid,
            "name" to contact.name,
            "phone" to contact.phone,
            "relationship" to contact.relationship,
            "timestamp" to contact.timestamp
        )
        contacts(uid).add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun listenContacts(
        uid: String,
        onChange: (List<ContactPayload>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        contacts(uid).orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) onError(error)
                else onChange(snapshot?.documents?.map { it.toContact() }.orEmpty())
            }

    // ---------- Emergencias ----------

    fun addEmergency(
        uid: String,
        emergency: EmergencyPayload,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = mapOf(
            "uid" to emergency.uid,
            "type" to emergency.type,
            "latitude" to emergency.latitude,
            "longitude" to emergency.longitude,
            "address" to emergency.address,
            "status" to emergency.status,
            "timestamp" to emergency.timestamp
        )
        emergencies(uid).add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun listenEmergencies(
        uid: String,
        onChange: (List<EmergencyPayload>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        emergencies(uid).orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) onError(error)
                else onChange(snapshot?.documents?.map { it.toEmergency() }.orEmpty())
            }

    // ---------- Mapeo manual (evita problemas de reflexión con data classes de Kotlin) ----------

    private fun DocumentSnapshot.str(field: String) = getString(field).orEmpty()

    private fun DocumentSnapshot.toUserProfile() = UserProfile(
        fullName = str("fullName"),
        phone = str("phone"),
        address = str("address"),
        email = str("email"),
        bloodType = str("bloodType"),
        allergies = str("allergies"),
        conditions = str("conditions"),
        medications = str("medications")
    )

    private fun DocumentSnapshot.toContact() = ContactPayload(
        id = id,
        ownerUid = str("ownerUid"),
        name = str("name"),
        phone = str("phone"),
        relationship = str("relationship"),
        timestamp = getLong("timestamp") ?: 0L
    )

    private fun DocumentSnapshot.toEmergency() = EmergencyPayload(
        id = id,
        uid = str("uid"),
        type = str("type"),
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        address = str("address"),
        status = str("status"),
        timestamp = getLong("timestamp") ?: 0L
    )

    companion object {
        private const val USERS = "users"
        private const val CONTACTS = "contacts"
        private const val EMERGENCIES = "emergencies"
    }
}
