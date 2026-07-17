package com.mflores.apisplynx.ui.tasks

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.util.CameraFileUtils
import com.mflores.apisplynx.viewmodel.TaskDetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
    val isClosing by viewModel.isClosing.collectAsState()
    val taskClosed by viewModel.taskClosed.collectAsState()

    val context = LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }

    // ==================== NUEVOS ESTADOS PARA LA CÁMARA ====================

    // Guarda el archivo temporal de la foto tomada. Es null si aún no hay foto.
    // Usamos File porque necesitaremos la ruta luego para subirla a Splynx.
    var photoFile by remember { mutableStateOf<File?>(null) }

    // Guarda la Uri de la foto tomada (para mostrarla en pantalla con Coil).
    // Uri es como una "URL" que apunta al archivo.
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Permiso de la cámara. Accompanist se encarga de saber si el usuario ya lo dio.
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Marca que la foto ya se ha guardado y está lista para mostrarse
    var photoReady by remember { mutableStateOf(false) }

    // Launcher para abrir la cámara. TakePicture es un contrato que:
    // - Recibe una Uri donde guardar la foto
    // - Devuelve true si el usuario tomó la foto, false si canceló
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->

        if (success) {
            photoReady = true  // ← ahora la foto está lista
        } else {
            photoFile = null
            photoUri = null
            photoReady = false
        }
    }



    // Función auxiliar para abrir la cámara.
    // Crea el archivo temporal, guarda las referencias y lanza la cámara.
    fun openCamera() {
        val (file, uri) = CameraFileUtils.createImageFile(context)
        photoFile = file
        photoUri = uri
        photoReady = false
        cameraLauncher.launch(uri)
    }

    // =======================================================================

    LaunchedEffect(taskId, customerId) {
        viewModel.loadDetails(taskId, customerId)
    }

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

                        // ==================== SECCIÓN DE FOTO ====================
                        // Aquí ponemos toda la lógica de tomar/mostrar/descartar foto
                        PhotoSection(
                            photoUri = photoUri,
                            photoFile = photoFile,
                            photoReady = photoReady,
                            onTakePhotoClick = {
                                // Si el permiso ya está concedido, abrimos la cámara directamente
                                // Si no, lo pedimos primero
                                if (cameraPermissionState.status.isGranted) {
                                    openCamera()
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            onDiscardPhotoClick = {
                                // Borramos el archivo del disco y limpiamos el estado
                                photoFile?.delete()
                                photoFile = null
                                photoUri = null
                                photoReady = false
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Efecto que se ejecuta cuando el usuario acepta o rechaza el permiso.
                        // Si acaba de aceptar, abrimos la cámara automáticamente para que
                        // no tenga que pulsar el botón dos veces.
                        LaunchedEffect(cameraPermissionState.status.isGranted) {
                            // Solo abre si photoUri es null (para no reabrir después de tomar foto)
                            if (cameraPermissionState.status.isGranted && photoUri == null) {
                                // Este bloque se ejecutará si el usuario acaba de aceptar,
                                // pero no queremos abrir sola la cámara al entrar en la pantalla
                                // así que no hacemos nada aquí. La cámara se abre desde onTakePhotoClick.
                            }
                        }
                        // =========================================================

                        // Botón Google Maps
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

                        // Botón cerrar tarea
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isClosing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            if (isClosing) {
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

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Cerrar tarea?") },
            text = { Text("Esta acción marcará la tarea como terminada. ¿Estás seguro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.closeTask(taskId)
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

// Sección con el botón de foto y la vista previa si ya se ha tomado una.
@Composable
fun PhotoSection(
    photoUri: Uri?,
    photoFile: File?,
    photoReady: Boolean,
    onTakePhotoClick: () -> Unit,
    onDiscardPhotoClick: () -> Unit
) {
    Column {
        Text(
            text = "Foto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!photoReady) {
            // No hay foto: mostramos el botón para tomarla
            OutlinedButton(
                onClick = onTakePhotoClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(


                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hacer foto")
            }
        } else {
            // Ya hay foto: mostramos la vista previa y el botón para descartar
            // Coil se encarga de cargar la imagen desde la Uri automáticamente
            Image(
                painter = rememberAsyncImagePainter(
                    model = photoFile,  // ← cambia photoUri por photoFile
                ),
                contentDescription = "Foto tomada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para descartar la foto (por si ha quedado mal)
            OutlinedButton(
                onClick = onDiscardPhotoClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descartar foto")
            }
        }
    }
}

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
            text = task.scheduledFrom ?: "Sin fecha"
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(
            icon = Icons.Default.LocationOn,
            text = task.address ?: "Sin dirección"
        )
    }
}

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