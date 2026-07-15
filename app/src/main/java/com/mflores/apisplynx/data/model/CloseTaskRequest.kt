package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

// Modelo que representa el body de la petición para cerrar una tarea
// Solo enviamos el campo "closed" con valor "1" para marcarla como cerrada
data class CloseTaskRequest(
    @SerializedName("closed") val closed: String = "1"
)
