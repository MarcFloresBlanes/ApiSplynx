package com.mflores.apisplynx.data.remote

import com.mflores.apisplynx.data.local.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que se encarga de inyectar el token de autenticación
 * en cada una de las peticiones que salen de la App hacia la API de Splynx.
 * 
 * @param sessionManager Gestor de la sesión local para recuperar el token guardado.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            sessionManager.accessToken.first()
        }

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Forzamos al servidor a que nos responda en JSON.
        requestBuilder.header("Accept", "application/json")

        // SIEMPRE incluimos Content-Type si hay un cuerpo en la petición (POST, PUT, etc)
        // Esto es vital para que Splynx v2 acepte el JSON del login
        if (originalRequest.method != "GET" && originalRequest.body != null) {
            requestBuilder.header("Content-Type", "application/json")
        }

        token?.let {
            val cleanToken = it.trim()
            if (cleanToken.isNotEmpty()) {
                // FORMATO ESTÁNDAR PARA SPLYNX 2.0 (JWT):
                requestBuilder.header("Authorization", "Splynx-EA (access_token=$cleanToken)")
            }
            
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
