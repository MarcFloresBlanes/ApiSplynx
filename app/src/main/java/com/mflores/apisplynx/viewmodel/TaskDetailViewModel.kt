package com.mflores.apisplynx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mflores.apisplynx.data.model.CustomerItem
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel para la pantalla de detalle de una tarea.
// Carga tanto los datos completos de la tarea como los del cliente asociado.
class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoginRepository(application)

    // Estado de la tarea cargada (con todos los campos: gps, descripción, etc.)
    private val _task = MutableStateFlow<TaskItem?>(null)
    val task: StateFlow<TaskItem?> = _task.asStateFlow()

    // Estado del cliente asociado
    private val _customer = MutableStateFlow<CustomerItem?>(null)
    val customer: StateFlow<CustomerItem?> = _customer.asStateFlow()

    // Estado de carga global (true mientras al menos una llamada esté en curso)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensaje de error si algo falla
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Carga la tarea y el cliente en paralelo
    fun loadDetails(taskId: Int, customerId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 1. Cargamos la tarea
                val taskResponse = repository.getTask(taskId)
                if (taskResponse.isSuccessful) {
                    _task.value = taskResponse.body()
                    println("DEBUG_TASK_DETAIL: Tarea cargada: ${taskResponse.body()?.title}")
                } else {
                    _errorMessage.value = "Error al cargar la tarea: ${taskResponse.code()}"
                }

                // 2. Cargamos el cliente (si la tarea tiene cliente asociado)
                if (customerId != 0) {
                    val customerResponse = repository.getCustomer(customerId)
                    if (customerResponse.isSuccessful) {
                        _customer.value = customerResponse.body()
                        println("DEBUG_CUSTOMER: Cliente cargado: ${customerResponse.body()?.name}")
                    } else {
                        println("DEBUG_CUSTOMER: Error ${customerResponse.code()}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG_TASK_DETAIL: Excepción: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
