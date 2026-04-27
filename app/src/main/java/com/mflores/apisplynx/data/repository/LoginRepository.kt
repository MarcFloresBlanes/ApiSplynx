package com.mflores.apisplynx.data.repository

import android.content.Context
import com.mflores.apisplynx.data.local.SessionManager
import com.mflores.apisplynx.data.model.LoginRequest
import com.mflores.apisplynx.data.model.LoginResponse
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.data.remote.RetrofitClient
import com.mflores.apisplynx.util.JwtUtils
import kotlinx.coroutines.flow.first
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
                // Decodificamos el JWT para extraer el ID del admin que viene dentro del token
                // y lo guardamos también en local para usarlo al filtrar las tareas
                JwtUtils.extractAdminId(token)?.let { adminId ->
                    sessionManager.saveAdminId(adminId)
                }
                RetrofitClient.clearInstance() // Fuerza recrear el cliente con el token nuevo
            }
        }

        return response
    }

    suspend fun getTasks(): Response<List<TaskItem>> {
        // Recuperamos el ID del admin que guardamos al hacer login
        // Si por algún motivo no existe usamos una cadena vacía
        val adminId = sessionManager.adminId.first() ?: ""

        // Hacemos la llamada a la API pasando el ID para que el servidor
        // solo nos devuelva las tareas de este administrador
        // Llamamos a la API con el filtro de assignee
        return RetrofitClient.getApiService(context).getTasks(assignee = adminId)
    }
}
