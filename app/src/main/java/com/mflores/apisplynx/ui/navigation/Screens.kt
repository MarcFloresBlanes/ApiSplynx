package com.mflores.apisplynx.ui.navigation

import kotlinx.serialization.Serializable

// Definimos las pantallas para la navegación con Safe Args
@Serializable
object Login

@Serializable
object Home

@Serializable
object Tasks
@Serializable
data class TaskDetail(
    val taskId: Int,
    val customerId: Int
)