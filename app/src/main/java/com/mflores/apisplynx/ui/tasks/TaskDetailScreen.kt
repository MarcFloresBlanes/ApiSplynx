package com.mflores.apisplynx.ui.tasks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.viewmodel.TaskDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    customerId: Int,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel()
) {
    val task by viewModel.task.collectAsState()
    val customer by viewModel.customer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // LocalContext.current nos da acceso al contexto para poder lanzar Intents
    // (necesario para abrir la app de Google Maps)
    val context = LocalContext.current

    // Cuando se abre la pantalla, lanzamos la carga de los datos
    LaunchedEffect(taskId, customerId) {
        viewModel.loadDetails(taskId, customerId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de tarea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                task != null -> {
                    // Hacemos la columna scrollable por si el contenido no cabe en pantalla
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Sección de la tarea
                        TaskInfoSection(task = task!!)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sección del cliente (solo si lo hemos cargado)
                        if (customer != null) {
                            CustomerInfoSection(customer = customer!!)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Botón para abrir Google Maps (solo si la tarea tiene GPS)
                        if (!task!!.gps.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    // Construimos un Intent que le pide al sistema que abra
                                    // una app capaz de mostrar coordenadas geográficas.
                                    // El esquema "geo:" es el estándar de Android para esto.
                                    val gpsUri = Uri.parse("geo:${task!!.gps}?q=${task!!.gps}")
                                    val intent = Intent(Intent.ACTION_VIEW, gpsUri)
                                    // setPackage fuerza que se abra concretamente Google Maps
                                    // si está instalada (sino se usa cualquier app de mapas)
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir en Google Maps")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sección con los datos principales de la tarea
@Composable
fun TaskInfoSection(task: TaskItem) {
    Column {
        // Título de la tarea
        Text(
            text = task.title ?: "Sin título",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fecha
        InfoRow(
            icon = Icons.Default.Schedule,
            text = task.dateStart ?: "Sin fecha"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dirección
        InfoRow(
            icon = Icons.Default.LocationOn,
            text = task.address ?: "Sin dirección"
        )
    }
}

// Sección con los datos del cliente
@Composable
fun CustomerInfoSection(customer: CustomerItem) {
    Column {
        Text(
            text = "Información del cliente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(
            icon = Icons.Default.Person,
            text = customer.name ?: "Sin nombre"
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(
            icon = Icons.Default.Phone,
            text = customer.phone ?: "Sin teléfono"
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(
            icon = Icons.Default.Email,
            text = customer.email ?: "Sin email"
        )
    }
}

// Componente reutilizable: una fila con icono + texto
@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}