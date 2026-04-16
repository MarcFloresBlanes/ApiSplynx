package com.mflores.apisplynx.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal que se muestra tras un inicio de sesión exitoso.
 * 
 * @param onLogout Callback que se ejecuta cuando el usuario pulsa el botón de cerrar sesión.
 * @param onGoToTasks Callback para navegar a la lista de tareas.
 */
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onGoToTasks: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido a la Home!",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Botón para ir a tareas
        Button(
            onClick = onGoToTasks,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(text = "Ver Mis Tareas")
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // Botón para cerrar sesión
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}
