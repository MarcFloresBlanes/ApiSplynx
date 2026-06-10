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
    @SerializedName("related_customer_id") val relatedCustomerId: Int?
)
