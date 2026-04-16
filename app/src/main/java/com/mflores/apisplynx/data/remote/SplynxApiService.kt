package com.mflores.apisplynx.data.remote

import com.mflores.apisplynx.data.model.LoginRequest
import com.mflores.apisplynx.data.model.LoginResponse
import com.mflores.apisplynx.data.model.TaskItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SplynxApiService {

    @POST("admin/auth/tokens")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Obtener listado de tareas (Scheduling)
    // El prefijo 'admin/' es obligatorio para acceder a recursos de administración
    // tras confirmarse error 500 sin él.
    @GET("admin/scheduling/tasks/")
    suspend fun getTasks(): Response<List<TaskItem>>
}
