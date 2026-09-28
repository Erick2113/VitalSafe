package com.example.vitalsafe.ui.screens.usuario

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.vitalsafe.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onProfileSaved: () -> Unit,
    onEditMedicalRecord: () -> Unit,
    onEditPersonalData: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val notRegistered = "Sin registrar"

    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("VitalSafePrefs", Context.MODE_PRIVATE) }
    // Leemos si el interruptor de seguridad está activado
    val isBiometricEnabled = sharedPreferences.getBoolean("use_biometrics", false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MI PERFIL", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF00ACC1))
            }
            state.errorMessage?.let { message ->
                Text(message, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF)),
                contentAlignment = Alignment.Center
            ) {
                Text(state.initials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.fullName.ifBlank { "Completa tu perfil" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            Text("CIUDADANO", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                border = BorderStroke(1.dp, Color(0xFFDC3545)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Sangre", tint = Color(0xFFDC3545))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("TIPO DE SANGRE", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        Text(state.bloodType.ifBlank { notRegistered }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text("INFORMACIÓN MÉDICA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                Spacer(modifier = Modifier.height(12.dp))

                MedicalItemCard("ALERGIAS", state.allergies.ifBlank { notRegistered }, Color(0xFFFFC107), Icons.Default.Warning)
                MedicalItemCard("PADECIMIENTOS", state.conditions.ifBlank { notRegistered }, Color(0xFF0D6EFD), Icons.Default.AddCircle)
                MedicalItemCard("MEDICAMENTOS", state.currentMedications.ifBlank { notRegistered }, Color(0xFF198754), Icons.Default.CheckCircle)
                MedicalItemCard("CONTACTO DE EMERGENCIA", state.emergencyContact.ifBlank { notRegistered }, Color(0xFFFD7E14), Icons.Default.Phone)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    // Botón del Expediente Médico
                    onClick = {
                        if (isBiometricEnabled) {
                            authenticateToEdit(context, onSuccess = onEditMedicalRecord)
                        } else {
                            onEditMedicalRecord()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar Expediente Médico", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "Datos", tint = Color(0xFF198754))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis datos", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        // LÓGICA DE SEGURIDAD 2: Lapicito de Datos Personales
                        IconButton(
                            onClick = {
                                if (isBiometricEnabled) {
                                    authenticateToEdit(context, onSuccess = onEditPersonalData)
                                } else {
                                    onEditPersonalData()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar datos", tint = Color.DarkGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileDataRow(Icons.Default.Person, "Nombre completo", state.fullName.ifBlank { notRegistered })
                    ProfileDataRow(Icons.Default.Phone, "Teléfono", state.phone.ifBlank { notRegistered })
                    ProfileDataRow(Icons.Default.LocationOn, "Dirección", state.address.ifBlank { notRegistered })
                    ProfileDataRow(Icons.Default.Email, "Correo electrónico", state.email.ifBlank { notRegistered })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = "Config", tint = Color(0xFF00ACC1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configuración y seguridad", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsRow(Icons.Default.Person, "Configuración de la cuenta")
                    SettingsRow(Icons.Default.Lock, "Seguridad", onClick = onNavigateToSecurity)
                    SettingsRow(Icons.Default.Info, "Centro de ayuda")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                border = BorderStroke(1.dp, Color(0xFFDC3545)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MedicalItemCard(title: String, value: String, stripeColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(stripeColor))
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = stripeColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(title, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
        }
    }
}

@Composable
fun ProfileDataRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color(0xFF555555))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF8F9FA)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00ACC1), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = "Ir", tint = Color.DarkGray)
    }
}


fun authenticateToEdit(context: Context, onSuccess: () -> Unit) {
    val fragmentActivity = context as? FragmentActivity
    if (fragmentActivity == null) {
        Toast.makeText(context, "Error interno", Toast.LENGTH_SHORT).show()
        return
    }

    val biometricManager = BiometricManager.from(context)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    if (biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess() // Si la huella es correcta, navega al formulario
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Edición cancelada", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Requerida")
            .setSubtitle("Verifica tu identidad para modificar tus datos sensibles")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    } else {

        Toast.makeText(context, "Tu teléfono no tiene seguridad configurada.", Toast.LENGTH_LONG).show()
    }
}