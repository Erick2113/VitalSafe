package com.example.vitalsafe.ui.screens.usuario

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitalsafe.ui.viewmodel.MedicalRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicalRecordScreen(
    viewModel: MedicalRecordViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Animación suave e interactiva para la barra
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        label = "progressAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar Expediente", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A)) // Azul VitalSafe
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Progreso de tu expediente", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            // BARRA DE PROGRESO
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = Color(0xFF00ACC1), // Turquesa
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(state.progress * 100).toInt()}% Completado",
                fontSize = 14.sp,
                color = Color(0xFF00ACC1),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.bloodType,
                onValueChange = { viewModel.updateField("blood", it) },
                label = { Text("Tipo de Sangre") },
                placeholder = { Text("Ej: O+, A-") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.allergies,
                onValueChange = { viewModel.updateField("allergies", it) },
                label = { Text("Alergias") },
                placeholder = { Text("Ej: Penicilina, Ninguna") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.conditions,
                onValueChange = { viewModel.updateField("conditions", it) },
                label = { Text("Padecimientos Crónicos") },
                placeholder = { Text("Ej: Asma, Diabetes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.medications,
                onValueChange = { viewModel.updateField("medications", it) },
                label = { Text("Medicamentos Actuales") },
                placeholder = { Text("Ej: Salbutamol") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onNavigateBack, // A futuro guardará en Firebase
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GUARDAR EXPEDIENTE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}