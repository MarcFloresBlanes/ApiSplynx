package com.mflores.apisplynx.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.viewmodel.TaskDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    customerId: Int,
    onBack: () -> Unit,
    // El ViewModel se crea automáticamente con esta línea
    viewModel: TaskDetailViewModel = viewModel()
) {
    // Recogemos los estados del ViewModel para que la UI se redibuje automáticamente
    val customer by viewModel.customer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // LaunchedEffect ejecuta su contenido una sola vez cuando la pantalla se abre
    // (o cuando cambia el customerId). Aquí disparamos la carga de los datos del cliente.
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
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
                // Mientras se carga, mostramos un círculo girando
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Si hay error, lo mostramos centrado
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                // Si todo va bien, mostramos los datos del cliente
                customer != null -> {
                    CustomerInfoSection(
                        customer = customer!!,
                        taskId = taskId,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

// Componente que muestra la información del cliente
// Lo separamos en una función aparte para que el código sea más limpio
@Composable
fun CustomerInfoSection(
    customer: CustomerItem,
    taskId: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Título de la sección
        Text(
            text = "Información del cliente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fila del nombre
        InfoRow(
            icon = Icons.Default.Person,
            text = customer.name ?: "Sin nombre"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fila del teléfono
        InfoRow(
            icon = Icons.Default.Phone,
            text = customer.phone ?: "Sin teléfono"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fila del email
        InfoRow(
            icon = Icons.Default.Email,
            text = customer.email ?: "Sin email"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // De momento dejamos el ID de la tarea visible para depurar
        // Lo quitaremos cuando añadamos el mapa
        Text(
            text = "ID de la tarea: $taskId",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// Componente reutilizable para mostrar una fila con icono + texto
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
