package com.example.vitalsafe.ui.screens.usuario

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitalsafe.location.LocationTracker
import com.example.vitalsafe.location.LocationTrackingService
import com.example.vitalsafe.ui.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember { mutableStateOf(LocationTracker.hasLocationPermission(context)) }
    var hasBackgroundPermission by remember { mutableStateOf(LocationTracker.hasBackgroundPermission(context)) }
    val cameraPositionState = rememberCameraPositionState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    // El permiso "Permitir todo el tiempo" debe pedirse aparte, después del de ubicación normal
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasBackgroundPermission = LocationTracker.hasBackgroundPermission(context) }
    )

    // Solo revisamos permisos aquí
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            // Android 13+ necesita permiso para mostrar la notificación del rastreo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // Con permiso concedido: arranca el rastreo continuo y pide una primera ubicación rápida para el mapa
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            LocationTrackingService.start(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateLocation(location)
                    }
                }
        }
    }

    // El motor del zoom: sigue al usuario cada vez que llega una nueva coordenada
    val loc by viewModel.currentLocation.collectAsState()
    var hasCenteredMap by remember { mutableStateOf(false) }
    LaunchedEffect(loc) {
        val current = loc ?: return@LaunchedEffect
        val latLng = LatLng(current.latitude, current.longitude)
        if (!hasCenteredMap) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latLng, 17f), // Nivel 17: Zoom perfecto a nivel de calle
                durationMs = 1500
            )
            hasCenteredMap = true
        } else {
            // Después del primer zoom se respeta el nivel que haya elegido el usuario
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng), durationMs = 800)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.triggerSOS() },
            enabled = !viewModel.isSending,
            modifier = Modifier.size(180.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                disabledContainerColor = Color(0xFFFFCC80)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            if (viewModel.isSending) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
            } else {
                Text("SOS", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = viewModel.sosStatus, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (hasLocationPermission && !hasBackgroundPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Para compartir tu ubicación con la app cerrada, elige \"Permitir todo el tiempo\".",
                        color = Color(0xFF856404),
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = { backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
                    ) {
                        Text("PERMITIR", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (hasLocationPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Se requiere permiso de ubicación para el mapa.", color = Color(0xFF856404))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}