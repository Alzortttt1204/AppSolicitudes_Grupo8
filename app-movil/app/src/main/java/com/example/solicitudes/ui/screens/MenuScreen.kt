package com.example.solicitudes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.solicitudes.viewmodel.SolicitudViewModel
import com.example.solicitudes.viewmodel.TipoCambioViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: SolicitudViewModel,
    tipoCambioViewModel: TipoCambioViewModel
) {
    val tipoCambio by tipoCambioViewModel.estado.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Menú principal") }) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta con datos de la API externa (tipo de cambio del día)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AttachMoney, contentDescription = null)
                        Text(
                            "Tipo de cambio de hoy",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    when {
                        tipoCambio.cargando -> Text("Cargando...")
                        tipoCambio.error != null -> {
                            Text(tipoCambio.error ?: "", color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { tipoCambioViewModel.cargarTipoCambio() }) {
                                Text("Reintentar")
                            }
                        }
                        else -> {
                            Text("Dólar: ${formatoPesos(tipoCambio.dolar)}")
                            Text("Euro: ${formatoPesos(tipoCambio.euro)}")
                            Text(
                                "Fuente: open.er-api.com",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { navController.navigate("lista") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                Text("Ver solicitudes", modifier = Modifier.padding(start = 8.dp))
            }

            Button(
                onClick = {
                    viewModel.nuevaSolicitud()
                    navController.navigate("formulario")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Nueva solicitud", modifier = Modifier.padding(start = 8.dp))
            }

            OutlinedButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// Muestra un valor como pesos chilenos, por ejemplo: $950,12
fun formatoPesos(valor: Double?): String {
    if (valor == null) return "-"
    return "$" + String.format(Locale("es", "CL"), "%,.2f", valor)
}
