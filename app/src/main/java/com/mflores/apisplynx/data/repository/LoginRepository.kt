package com.mflores.apisplynx.data.repository

import android.content.Context
import com.mflores.apisplynx.data.model.LoginRequest
import com.mflores.apisplynx.data.model.LoginResponse
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.data.remote.RetrofitClient
import retrofit2.Response

/**
 * Repositorio encargado de gestionar las operaciones de autenticación.
 * 
 * @param context Se recibe el contexto para poder inicializar el SessionManager
 * y recuperar/guardar tokens de forma segura.
 */
class LoginRepository(private val context: Context) {

    // Obtenemos el servicio de API pasando el contexto para que el interceptor
    // pueda acceder a los tokens guardados.
    private val apiService = RetrofitClient.getApiService(context)

    suspend fun login(login: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(
            authType = "admin", // Splynx 2.0 requiere especificar el tipo de autenticación
            login = login,
            password = password
        )

        return apiService.login(request)
    }

    /**
     * Obtiene el listado de tareas asignadas al administrador desde el endpoint de Scheduling.
     */
    suspend fun getTasks(): Response<List<TaskItem>> {
        return apiService.getTasks()
    }
}
