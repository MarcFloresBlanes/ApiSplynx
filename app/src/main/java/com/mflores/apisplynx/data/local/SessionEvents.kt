package com.mflores.apisplynx.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Objeto global (singleton) para notificar eventos relacionados con la sesión.
// Lo usamos para avisar a la UI cuando el token ha expirado y hay que volver al login.
object SessionEvents {

    // SharedFlow es como un "canal" donde podemos emitir eventos
    // y cualquier pantalla que esté escuchando los recibirá
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    // Función para emitir el evento de "sesión expirada"
    // tryEmit no requiere coroutine y devuelve true si se emitió correctamente
    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
