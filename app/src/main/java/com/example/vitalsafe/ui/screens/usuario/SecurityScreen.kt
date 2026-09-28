package com.example.vitalsafe.ui.screens.usuario

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("VitalSafePrefs", Context.MODE_PRIVATE) }

    var isBiometricEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("use_biometrics", false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguridad", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
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
                .padding(24.dp)
        ) {
            Text(
                text = "Protección de la App",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Añade una capa extra de seguridad para proteger tus datos médicos de modificaciones no autorizadas.",
                color = Color.DarkGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00ACC1), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bloqueo por Huella / PIN", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Solicitar al editar datos", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { isChecked ->
                            // En lugar de cambiarlo directo, llamamos a la seguridad primero
                            authenticateToToggleSecurity(context) {
                                isBiometricEnabled = isChecked
                                sharedPreferences.edit().putBoolean("use_biometrics", isChecked).apply()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00ACC1),
                            checkedTrackColor = Color(0xFFB2EBF2)
                        )
                    )
                }
            }
        }
    }
}

// Función que revisa si el cel tiene PIN/Huella y lanza la pantalla negra
fun authenticateToToggleSecurity(context: Context, onSuccess: () -> Unit) {
    val fragmentActivity = context as? FragmentActivity
    if (fragmentActivity == null) {
        Toast.makeText(context, "Error de sistema", Toast.LENGTH_SHORT).show()
        return
    }

    val biometricManager = BiometricManager.from(context)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    // Verificamos si el teléfono tiene algún tipo de seguridad activada
    when (biometricManager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Todo en orden, lanzamos el cuadro de verificación
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                fragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess() // ¡Permiso concedido! Cambiamos el interruptor
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(context, "Acción cancelada", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Seguridad VitalSafe")
                .setSubtitle("Verifica tu identidad para cambiar esta configuración")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> {
            // Si el teléfono no tiene ni PIN ni Huella, le avisamos al usuario
            Toast.makeText(context, "Debes configurar un PIN o Huella en los ajustes de tu teléfono primero.", Toast.LENGTH_LONG).show()
        }
    }
}