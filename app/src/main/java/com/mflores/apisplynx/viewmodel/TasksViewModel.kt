package com.mflores.apisplynx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mflores.apisplynx.data.model.TaskItem
import com.mflores.apisplynx.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de la lista de tareas.
 */
class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoginRepository(application)

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /*
    init {
        loadTasks()
    }
    */

    /**
     * Carga las tareas desde la API de Splynx.
     */
    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getTasks()
                if (response.isSuccessful) {
                    _tasks.value = response.body() ?: emptyList()
                    println("DEBUG_TASKS: Tareas cargadas con éxito: ${_tasks.value.size}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG_TASKS: Error en la petición: ${response.code()} - $errorBody")
                    // Splynx suele devolver un JSON con un campo "message" o "error"
                    _errorMessage.value = "Error ${response.code()}: $errorBody"
                }
            } catch (e: Exception) {
                println("DEBUG_TASKS: Excepción capturada: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
