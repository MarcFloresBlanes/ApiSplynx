package com.mflores.apisplynx.ui.tasks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // Nuevos estados relacionados con el cierre de la tarea
    val isClosing by viewModel.isClosing.collectAsState()
    val taskClosed by viewModel.taskClosed.collectAsState()

    val context = LocalContext.current

    // Estado local para controlar si el diálogo de confirmación está visible o no
    // Usamos remember + mutableStateOf porque es un estado que solo interesa a la UI
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Cargamos los datos al entrar
    LaunchedEffect(taskId, customerId) {
        viewModel.loadDetails(taskId, customerId)
    }

    // Cuando la tarea se cierra correctamente, volvemos automáticamente atrás
    // Este LaunchedEffect se dispara cuando taskClosed pasa de false a true
    LaunchedEffect(taskClosed) {
        if (taskClosed) {
            onBack()
        }
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
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        TaskInfoSection(task = task!!)

                        Spacer(modifier = Modifier.height(24.dp))

                        if (customer != null) {
                            CustomerInfoSection(customer = customer!!)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Botón para abrir Google Maps (solo si hay GPS)
                        if (!task!!.gps.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    val gpsUri = Uri.parse("geo:${task!!.gps}?q=${task!!.gps}")
                                    val intent = Intent(Intent.ACTION_VIEW, gpsUri)
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

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Botón para cerrar la tarea
                        // Al pulsar mostramos el diálogo de confirmación en vez de cerrar directamente
                        // enabled = !isClosing → mientras se está cerrando, el botón se deshabilita
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isClosing,
                            // Color verde para transmitir "acción positiva/completar"
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            if (isClosing) {
                                // Mientras se cierra, mostramos un spinner pequeño
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cerrar tarea")
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    // Solo se muestra cuando showConfirmDialog es true
    if (showConfirmDialog) {
        AlertDialog(
            // Al pulsar fuera del diálogo o el botón atrás, se cierra sin hacer nada
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Cerrar tarea?") },
            text = { Text("Esta acción marcará la tarea como terminada. ¿Estás seguro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false  // Cerramos el diálogo
                        viewModel.closeTask(taskId)  // Y llamamos a la API
                    }
                ) {
                    Text("Sí, cerrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Sección con los datos principales de la tarea
@Composable
fun TaskInfoSection(task: TaskItem) {
    Column {
        Text(
            text = task.title ?: "Sin título",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(
            icon = Icons.Default.Schedule,
            text = task.dateStart ?: "Sin fecha"
        )

        Spacer(modifier = Modifier.height(12.dp))

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