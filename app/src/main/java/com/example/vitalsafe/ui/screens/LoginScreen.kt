package com.example.vitalsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitalsafe.ui.viewmodel.AuthState
import com.example.vitalsafe.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }

    // Colores basados en tu diseño de Figma
    val primaryBlue = Color(0xFF175973)
    val backgroundColor = Color(0xFFF4F5F7)

    Box(modifier = Modifier.fillMaxSize()) {
        // FONDO DIVIDIDO (Azul arriba, Gris claro abajo)
        Column(modifier = Modifier.fillMaxSize()) {
            // Parte superior azul
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(primaryBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VitalSafe SV",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Respuesta, inmediata, vidas seguras",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                // Espacio extra para que el texto no choque con la tarjeta
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Parte inferior gris clara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .background(backgroundColor)
            )
        }

        // TARJETA FLOTANTE CENTRAL
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = 20.dp), // Baja la tarjeta un poco para centrarla mejor visualmente
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // CAMPO: CORREO ELECTRÓNICO
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Correo Electrónico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ejemplo@correo.com", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // CAMPO: CONTRASEÑA
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Contraseña",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Person else Icons.Default.Lock
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = "Mostrar contraseña", tint = Color.Gray)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // OLVIDASTE CONTRASEÑA
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = primaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* Lógica futura */ }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // MENSAJE DE ERROR
                    if (authState is AuthState.Error) {
                        Text(text = (authState as AuthState.Error).message, color = Color.Red, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // BOTÓN INICIAR SESIÓN
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TEXTO DE REGISTRO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "¿No tienes cuenta? ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Regístrate aquí",
                            color = primaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }
        }
    }
}