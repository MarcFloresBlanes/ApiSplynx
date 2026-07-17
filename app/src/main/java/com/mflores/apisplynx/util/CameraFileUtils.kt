package com.mflores.apisplynx.util

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Utilidad para crear archivos temporales donde guardar las fotos de la cámara.
object CameraFileUtils {

    // Crea un archivo vacío donde la cámara guardará la foto y devuelve
    // dos cosas: el File real (para leerlo luego) y su Uri (para pasársela a la cámara).
    fun createImageFile(context: Context): Pair<File, android.net.Uri> {
        // Nombre único basado en la fecha/hora para evitar colisiones
        // Ejemplo: JPEG_20260716_143025_
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Carpeta "Pictures" dentro del directorio privado externo de la app.
        // Esta ruta coincide con la que declaramos en file_paths.xml
        val storageDir = File(context.getExternalFilesDir(null), "Pictures").apply {
            // mkdirs crea la carpeta si no existe (sin dar error si ya existe)
            if (!exists()) mkdirs()
        }

        // Crea el archivo vacío .jpg dentro de esa carpeta
        val file = File.createTempFile(imageFileName, ".jpg", storageDir)

        // Convertimos el File a Uri usando el FileProvider que configuramos en el Manifest
        // La Uri es lo que le pasamos a la cámara (una URL segura tipo content://)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",  // Debe coincidir con las authorities del Manifest
            file
        )

        return Pair(file, uri)
    }
}
