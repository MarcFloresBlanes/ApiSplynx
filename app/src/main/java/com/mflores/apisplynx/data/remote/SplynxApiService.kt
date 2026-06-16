package com.mflores.apisplynx.data.remote

import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.data.model.LoginRequest
import com.mflores.apisplynx.data.model.LoginResponse
import com.mflores.apisplynx.data.model.TaskItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SplynxApiService {

    @POST("admin/auth/tokens")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // El parámetro de filtro de Splynx tiene formato especial: main_attributes[assignee]
// El @Query("main_attributes[assignee]") le dice a Retrofit que añada
// ese parámetro literal a la URL: ?main_attributes[assignee]=adminID
    @GET("admin/scheduling/tasks")
    suspend fun getTasks(
        @Query("main_attributes[assignee]") assignee: String
    ): Response<List<TaskItem>>

    // Endpoint para obtener los datos de un cliente concreto por su ID
// La URL queda como: admin/customers/customer/1065 (donde 1065 es el id)
// @Path inserta el valor del parámetro dentro de la URL, en el lugar de {id}
    @GET("admin/customers/customer/{id}")
    suspend fun getCustomer(
        @Path("id") customerId: Int
    ): Response<CustomerItem>
}
