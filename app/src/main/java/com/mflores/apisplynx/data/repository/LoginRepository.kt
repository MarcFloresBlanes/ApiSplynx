package com.mflores.apisplynx.data.repository

import android.content.Context
import com.mflores.apisplynx.data.local.SessionManager
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

    private val apiService = RetrofitClient.getApiService(context)
    private val sessionManager = SessionManager(context)

    suspend fun login(login: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(
            authType = "admin",
            login = login,
            password = password
        )

        val response = apiService.login(request)

        // Si el login es exitoso, guardamos el token y reiniciamos el cliente
        if (response.isSuccessful) {
            response.body()?.effectiveToken?.let { token ->
                sessionManager.saveAccessToken(token)
                RetrofitClient.clearInstance() // Fuerza recrear el cliente con el token nuevo
            }
        }

        return response
    }

    suspend fun getTasks(): Response<List<TaskItem>> {
        return RetrofitClient.getApiService(context).getTasks() // Usa siempre la instancia más reciente
    }
}
