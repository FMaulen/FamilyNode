package com.pointer.familynode.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pointer.familynode.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre") },
            isError = uiState.errors.name != null,
            supportingText = { uiState.errors.name?.let { Text(it) } }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo") },
            isError = uiState.errors.email != null,
            supportingText = { uiState.errors.email?.let { Text(it) } }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onClaveChange,
            label = { Text("Contraseña") },
            isError = uiState.errors.password != null,
            supportingText = { uiState.errors.password?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.registrar() }) {
            Text("Registrar")
        }
    }

    LaunchedEffect(uiState.succesfullSignUp) {
        if (uiState.succesfullSignUp) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}
