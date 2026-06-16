package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

// Modelo que representa un cliente devuelto por la API de Splynx
// Solo incluimos los campos que vamos a mostrar en la pantalla de detalle
// Todos son nullable por si la API devuelve null en alguno (defensive coding)
data class CustomerItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    // El teléfono puede venir con varios números separados por coma (ej: "34639293110,34676527872")
    @SerializedName("phone") val phone: String?
)