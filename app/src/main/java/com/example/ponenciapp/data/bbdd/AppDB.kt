package com.example.ponenciapp.data.bbdd

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.dao.AsistenciaDao
import com.example.ponenciapp.data.bbdd.dao.EventoDao
import com.example.ponenciapp.data.bbdd.dao.ParticipanteDao
import com.example.ponenciapp.data.bbdd.dao.PonenciaDao
import com.example.ponenciapp.data.bbdd.dao.ValoracionDao
import com.example.ponenciapp.data.bbdd.entities.AsistenciaData
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.data.bbdd.entities.PonenciaData
import com.example.ponenciapp.data.bbdd.entities.ValoracionData

@Database(
    entities = [
        ParticipanteData::class,
        EventoData::class,
        PonenciaData::class,
        AsistenciaData::class,
        ValoracionData::class
    ],
    version = Estructura.DB.VERSION,
    exportSchema = false
)

abstract class AppDB : RoomDatabase() {
    abstract fun participanteDao(): ParticipanteDao
    abstract fun eventoDao(): EventoDao
    abstract fun ponenciaDao(): PonenciaDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun valoracionDao(): ValoracionDao
}