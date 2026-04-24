package com.mflores.apisplynx.data.remote

import android.content.Context
import com.mflores.apisplynx.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de Retrofit centralizado. 
 * Provee la instancia de la API configurada con interceptores de seguridad y log.
 */
object RetrofitClient {
    // Volvemos a la versión 2.0 de la API
    private const val BASE_URL = "https://clientes.acerko.com/api/2.0/"
    
    // Guardamos una instancia única (Singleton) para no recrear el cliente en cada llamada
    private var apiService: SplynxApiService? = null

    /**
     * Devuelve el servicio de API configurado.
     * @param context Necesario para que el AuthInterceptor pueda leer el token de DataStore.
     */
    fun getApiService(context: Context): SplynxApiService {
        return apiService ?: synchronized(this) {
            val sessionManager = SessionManager(context)
            
            // Interceptor para ver las peticiones y respuestas en el Logcat (depuración)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Configuramos el cliente HTTP con nuestros interceptores
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)  // ← añade esto
                .readTimeout(30, TimeUnit.SECONDS)     // ← añade esto
                .writeTimeout(30, TimeUnit.SECONDS)    // ← añade esto
                .build()

            // Construimos la instancia de Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(SplynxApiService::class.java)
            apiService = service
            service
        }
    }

    fun clearInstance() {
        apiService = null
    }

}
