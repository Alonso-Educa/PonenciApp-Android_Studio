package com.example.ponenciapp.data.bbdd

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.dao.*
import com.example.ponenciapp.data.bbdd.entities.*

@Database(
    entities = [
        UsuarioData::class,
        EventoData::class,
        PonenciaData::class,
        AsistenciaData::class,
        ValoracionData::class,
        MensajeData::class
    ],
    version = Estructura.DB.VERSION,
    exportSchema = false
)

abstract class AppDB : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun eventoDao(): EventoDao
    abstract fun ponenciaDao(): PonenciaDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun valoracionDao(): ValoracionDao
    abstract fun mensajeDao(): MensajeDao
}