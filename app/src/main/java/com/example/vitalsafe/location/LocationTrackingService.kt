package com.example.vitalsafe.location

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.vitalsafe.MainActivity
import com.example.vitalsafe.data.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth

/**
 * Servicio en primer plano que sigue recibiendo coordenadas aunque la app
 * esté minimizada o cerrada, y las sube a users/{uid}.lastLocation.
 */
class LocationTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private val repository = UserRepository()
    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onNewLocation(it) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            // Android 12+ puede bloquear el arranque si el sistema lo reinicia sin permiso de segundo plano
            Log.w(TAG, "No se pudo iniciar el rastreo en primer plano", e)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!LocationTracker.hasLocationPermission(this) || FirebaseAuth.getInstance().currentUser == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startTracking()
        // START_STICKY: si el sistema mata el servicio, lo vuelve a levantar
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (isTracking) return
        isTracking = true

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && LocationTracker.location.value == null) onNewLocation(location)
        }
    }

    private fun onNewLocation(location: Location) {
        LocationTracker.update(location)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            stopSelf()
            return
        }
        repository.updateLastLocation(user.uid, location.latitude, location.longitude)
    }

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        isTracking = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rastreo de ubicación",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Mantiene tu ubicación actualizada para alertas SOS" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("VitalSafe está protegiéndote")
        .setContentText("Compartiendo tu ubicación para alertas de emergencia")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()

    companion object {
        private const val TAG = "LocationTracking"
        private const val CHANNEL_ID = "location_tracking"
        private const val NOTIFICATION_ID = 1001

        // Actualiza cada ~10 s y solo si el usuario se movió ~10 m (ahorra batería y escrituras)
        private const val UPDATE_INTERVAL_MS = 10_000L
        private const val FASTEST_INTERVAL_MS = 5_000L
        private const val MIN_DISTANCE_METERS = 10f

        // Solo debe llamarse con la app visible (Android 12+ no permite iniciarlo desde segundo plano)
        fun start(context: Context) {
            if (!LocationTracker.hasLocationPermission(context)) return
            if (FirebaseAuth.getInstance().currentUser == null) return
            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackingService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
            LocationTracker.clear()
        }
    }
}
