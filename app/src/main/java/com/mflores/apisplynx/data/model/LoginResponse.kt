package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para el inicio de sesión en Splynx 2.0.
 * La versión 2.0 utiliza tokens JWT (access_token) en lugar de una auth_key simple.
 */
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String?, // El token que usaremos para futuras peticiones
    @SerializedName("token") val tokenAlt: String?,          // Algunos servidores usan 'token' en lugar de 'access_token'
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("error") val error: ApiError? // Estructura de error detallada que devuelve Splynx
) {
    /**
     * Propiedad que devuelve el token disponible, priorizando 'access_token'.
     */
    val effectiveToken: String? get() = accessToken ?: tokenAlt
}

/**
 * Representa un error devuelto por la API de Splynx cuando la petición no es exitosa.
 */
data class ApiError(
    @SerializedName("code") val code: Int,
    @SerializedName("internal_code") val internalCode: String,
    @SerializedName("message") val message: String
)
