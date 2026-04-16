package com.mflores.apisplynx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mflores.apisplynx.data.local.SessionManager
import com.mflores.apisplynx.data.repository.LoginRepository
import com.google.gson.Gson
import com.mflores.apisplynx.data.model.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoginRepository(application)
    private val sessionManager = SessionManager(application)

    // Cambiado de _email a _username
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Cambiado a onUsernameChanged
    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun login(onSuccess: () -> Unit) {
        _errorMessage.value = null
        
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Por favor, rellena todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Realizamos la petición al repositorio
                val response = repository.login(_username.value, _password.value)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    // Si la respuesta es exitosa y contiene el token, lo guardamos y navegamos a Home
                    val token = loginResponse?.effectiveToken
                    if (token != null) {
                        sessionManager.saveAccessToken(token)
                        onSuccess()
                    } else {
                        _errorMessage.value = "Respuesta de servidor inválida"
                    }
                } else {
                    // EXPLICACIÓN DEL FIX: Cuando la API devuelve un error (ej: 401 Unauthorized),
                    // el mensaje de error viene en el 'errorBody'. Lo extraemos y lo convertimos
                    // a nuestro objeto LoginResponse para mostrar el mensaje real al usuario.
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = try {
                        Gson().fromJson(errorBody, LoginResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    _errorMessage.value = errorResponse?.error?.message ?: "Usuario o contraseña incorrectos"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
