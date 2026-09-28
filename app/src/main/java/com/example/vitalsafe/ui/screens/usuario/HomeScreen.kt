package com.example.vitalsafe.ui.screens.usuario

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
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

    var hasLocationPermission by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    // Solo revisamos permisos aquí
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // EL TRUCO: Apenas el sistema sepa que tiene permisos, fuerza al satélite a buscar tu ubicación real
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateLocation(location)
                    }
                }
        }
    }

    // El motor del zoom
    val loc = viewModel.currentLocation
    LaunchedEffect(loc) {
        if (loc != null) {
            val latLng = LatLng(loc.latitude, loc.longitude)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latLng, 17f), // Nivel 17: Zoom perfecto a nivel de calle
                durationMs = 1500
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (hasLocationPermission) {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                viewModel.updateLocation(location)
                                viewModel.triggerSOS()
                            }
                        }
                }
            },
            modifier = Modifier.size(180.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text("SOS", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = viewModel.sosStatus, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

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