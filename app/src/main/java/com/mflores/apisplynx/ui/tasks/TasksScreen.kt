package com.mflores.apisplynx.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.viewmodel.TasksViewModel
import androidx.compose.runtime.LaunchedEffect

/**
 * Pantalla que muestra el listado de tareas asignadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = viewModel(),
    onLogout: () -> Unit,
    onTaskClick: (taskId: Int, customerId: Int) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Recarga las tareas cada vez que la pantalla vuelve a ser visible
    // LaunchedEffect(Unit) se ejecuta al entrar en la pantalla,
    // incluyendo cuando volvemos del detalle tras cerrar una tarea
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (tasks.isEmpty()) {
                Text(
                    text = "No tienes tareas asignadas",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            // Al pulsar la card pasamos el ID de la tarea y el del cliente asociado
                            // Los ?: 0 son por si vienen null desde la API
                            onClick = {
                                onTaskClick(
                                    task.id ?: 0,
                                    task.relatedCustomerId ?: 0
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    onClick: () -> Unit  // ← nuevo parámetro
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },  // ← hace la card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title ?: "Sin título",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Formateamos la fecha (asumiendo formato YYYY-MM-DD HH:MM:SS)
                Text(
                    text = task.scheduledFrom ?: "Sin fecha",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.address ?: "Sin dirección especificada",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
