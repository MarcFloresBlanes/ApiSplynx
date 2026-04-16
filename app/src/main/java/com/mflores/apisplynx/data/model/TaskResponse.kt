package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para las tareas de Splynx (Scheduling).
 */
data class TaskItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("date_start") val dateStart: String, // Formato: YYYY-MM-DD HH:MM:SS
    @SerializedName("address") val address: String?
)
