package com.mflores.apisplynx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel para la pantalla de detalle de una tarea.
// Se encarga de cargar la información del cliente asociado a la tarea.
class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio que hace las llamadas a la API
    private val repository = LoginRepository(application)

    // Estado del cliente cargado (null mientras no se cargue o si falla)
    private val _customer = MutableStateFlow<CustomerItem?>(null)
    val customer: StateFlow<CustomerItem?> = _customer.asStateFlow()

    // Estado de carga, para mostrar un spinner mientras se hace la llamada
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensaje de error si algo falla
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Carga los datos del cliente desde la API
    // viewModelScope hace que la coroutine se cancele automáticamente si el ViewModel se destruye
    fun loadCustomer(customerId: Int) {
        // Si el ID es 0 significa que la tarea no tiene cliente asociado, no hacemos nada
        if (customerId == 0) {
            _errorMessage.value = "Esta tarea no tiene cliente asociado"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getCustomer(customerId)
                if (response.isSuccessful) {
                    _customer.value = response.body()
                    println("DEBUG_CUSTOMER: Cliente cargado: ${response.body()?.name}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG_CUSTOMER: Error ${response.code()} - $errorBody")
                    _errorMessage.value = "Error ${response.code()}: $errorBody"
                }
            } catch (e: Exception) {
                println("DEBUG_CUSTOMER: Excepción: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
