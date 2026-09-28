package com.example.vitalsafe.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitalsafe.ui.viewmodel.EditPersonalDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalDataScreen(
    viewModel: EditPersonalDataViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val blackTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Color(0xFF1E3A8A),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color(0xFF1E3A8A),
        unfocusedLabelColor = Color.Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar Mis Datos", color = Color.White, fontSize = 18.sp) },
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
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF198754))
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Modifica tu información de contacto", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.fullName,
                enabled = !state.isLoading && !state.isSaving,
                onValueChange = { viewModel.updateField("name", it) },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.phone,
                enabled = !state.isLoading && !state.isSaving,
                onValueChange = { viewModel.updateField("phone", it) },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.address,
                enabled = !state.isLoading && !state.isSaving,
                onValueChange = { viewModel.updateField("address", it) },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.email,
                enabled = !state.isLoading && !state.isSaving,
                onValueChange = { viewModel.updateField("email", it) },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = blackTextFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            state.errorMessage?.let { message ->
                Text(text = message, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.saveData(onSaved = onNavigateBack) },
                enabled = !state.isLoading && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
