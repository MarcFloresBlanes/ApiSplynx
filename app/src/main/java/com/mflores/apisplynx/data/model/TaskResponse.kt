package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para las tareas de Splynx (Scheduling).
 */
data class TaskItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("date_start") val dateStart: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("related_customer_id") val relatedCustomerId: Int?,
    // Coordenadas GPS de la tarea en formato "lat,lng" (ej: "41.70505827,2.87528414")
    @SerializedName("gps") val gps: String?,
    // Descripción de la tarea (puede contener HTML)
    @SerializedName("description") val description: String?
)
