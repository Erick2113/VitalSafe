package com.example.vitalsafe.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vitalsafe.ui.viewmodel.AuthViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var duiNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isScanning by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) isScanning = true else Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF1E3A8A))
            }
            Text(
                text = "Crear Cuenta",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visor de cámara
        if (isScanning && hasCameraPermission) {
            Text(
                "Apunta la cámara al frente de tu DUI",
                color = Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        val executor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    textRecognizer.process(image)
                                        .addOnSuccessListener { visionText ->
                                            val fullText = visionText.text
                                            val duiPattern = Regex("""\b\d{8}-\d\b""")
                                            val match = duiPattern.find(fullText)

                                            if (match != null) {
                                                duiNumber = match.value
                                                val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text.trim() }
                                                val forbiddenWords = listOf("REPUBLICA", "SALVADOR", "DOCUMENTO", "IDENTIDAD", "APELLIDOS", "NOMBRES", "SEXO", "LUGAR", "FECHA", "NACIMIENTO", "ESTADO", "FAMILIAR", "PROFESION", "CONOCIDO")

                                                val nameCandidates = lines.filter { line ->
                                                    line.length > 5 &&
                                                    !line.any { it.isDigit() } && // Que no tenga números
                                                    forbiddenWords.none { forbidden -> line.contains(forbidden, ignoreCase = true) }
                                                }

                                                if (nameCandidates.isNotEmpty()) {
                                                    fullName = nameCandidates.take(2).joinToString(" ")
                                                }
                                                isScanning = false
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isScanning = false }) {
                Text("Cancelar Escáner", color = Color.Red)
            }
        } else {
            Button(
                onClick = { if (hasCameraPermission) isScanning = true else permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear DUI para Llenado Automático", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CAMPOS CON LETRA FORZADA A NEGRO
        val blackTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black, // Forzado a negro
            unfocusedTextColor = Color.Black, // Forzado a negro
            focusedBorderColor = Color(0xFF1E3A8A),
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color(0xFF1E3A8A),
            unfocusedLabelColor = Color.Gray
        )

        OutlinedTextField(
            value = duiNumber,
            onValueChange = { duiNumber = it },
            label = { Text("Número de DUI") },
            placeholder = { Text("00000000-0") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = blackTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = blackTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Número de Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = blackTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = blackTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = blackTextFieldColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (duiNumber.isBlank() || email.isBlank() || password.isBlank() || fullName.isBlank()) {
                    Toast.makeText(context, "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.register(email, password, fullName, duiNumber, phone)
                    onRegisterSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF175973)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTRARSE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
