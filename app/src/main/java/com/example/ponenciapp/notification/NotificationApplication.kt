package com.example.ponenciapp.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class NotificationApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.O) // Android 8 (Oreo) o superior
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O) // Android 8 (Oreo) o superior
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "notification_channel_id", // ID único (como dirección)
            "Notificaciones", // Nombre que ve el usuario
            NotificationManager.IMPORTANCE_HIGH // Prioridad (sonido + LED + pantalla
            // bloqueada +prioridad alta en la lista)
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel) // Creamos el canal
    }
}

