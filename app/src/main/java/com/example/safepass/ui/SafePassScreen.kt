package com.example.safepass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.safepass.logic.procesarRegistro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePassScreen() {
    var nombre by remember { mutableStateOf("") }
    var edadTexto by remember { mutableStateOf("") }
    var tipoEntrada by remember { mutableStateOf("General") }
    var estado by remember { mutableStateOf<RegistroState>(RegistroState.Idle) }

    val tipos = listOf("General", "VIP", "Estudiante")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("SafePass 2026 - TechEvent") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del asistente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = edadTexto,
                onValueChange = { edadTexto = it },
                label = { Text("Edad") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de entrada", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tipos.forEach { tipo ->
                    FilterChip(
                        selected = tipoEntrada == tipo,
                        onClick = { tipoEntrada = tipo },
                        label = { Text(tipo) }
                    )
                }
            }

            Button(
                onClick = {
                    estado = procesarRegistro(nombre, edadTexto, tipoEntrada) { asistente ->
                        when (asistente.tipoEntrada) {
                            "Estudiante" -> 20
                            "VIP" -> 10
                            else -> 0
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }

            Spacer(Modifier.height(8.dp))

            when (val s = estado) {
                is RegistroState.Idle -> {
                    Text(
                        "Ingrese los datos del asistente para iniciar el registro.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is RegistroState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "✅ Registro exitoso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(s.resumen)
                        }
                    }
                }

                is RegistroState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Error de registro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(s.mensaje)
                        }
                    }
                }
            }
        }
    }
}