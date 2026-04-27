package com.mflores.apisplynx.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        // Nueva clave para guardar el ID del admin en el DataStore (almacenamiento local)
        private val ADMIN_ID = stringPreferencesKey("admin_id")
    }

    // Guardar el token
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    // Obtener el token como un Flow
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    // Borrar sesión
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Guarda el ID del administrador en el almacenamiento local del dispositivo
    suspend fun saveAdminId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[ADMIN_ID] = id
        }
    }
    // Devuelve el ID del admin como un Flow (se actualiza automáticamente si cambia)
    val adminId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ADMIN_ID]
    }

}
