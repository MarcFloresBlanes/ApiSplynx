package com.mflores.apisplynx.util

// Objeto utilitario para trabajar con tokens JWT
// Un JWT tiene 3 partes separadas por puntos: header.payload.signature
// Nosotros solo necesitamos el payload (parte del medio) que contiene los datos del usuario
object JwtUtils {
    fun extractAdminId(token: String): String? {
        return try {
            // Dividimos el token por los puntos y cogemos la parte del medio (índice 1)
            val payload = token.split(".")[1]

            // El payload está codificado en Base64, lo decodificamos a texto normal
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))

            // El texto decodificado es un JSON, lo arreglamos para poder leer sus campos
            val json = org.json.JSONObject(decoded)

            // Extraemos el campo "id" que es el ID del administrador y lo convertimos a String
            json.getInt("id").toString()
        } catch (e: Exception) {
            // Si algo falla (token malformado, campo no existe, etc.) devolvemos null
            null
        }
    }
}
