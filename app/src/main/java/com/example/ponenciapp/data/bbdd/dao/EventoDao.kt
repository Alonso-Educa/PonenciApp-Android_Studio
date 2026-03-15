package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ponenciapp.data.bbdd.entities.EventoData

@Dao
interface EventoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evento: EventoData)

    @Query("SELECT * FROM evento WHERE idEvento = :id LIMIT 1")
    suspend fun getEventoPorId(id: String): EventoData?

    @Query("SELECT * FROM evento")
    suspend fun getTodosEventos(): List<EventoData>

    @Query("DELETE FROM evento WHERE idEvento = :id")
    suspend fun eliminar(id: String)
}