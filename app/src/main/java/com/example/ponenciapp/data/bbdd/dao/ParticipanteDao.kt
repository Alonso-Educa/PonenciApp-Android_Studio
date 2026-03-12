package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData

@Dao
interface ParticipanteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(participante: ParticipanteData)

    @Query("SELECT * FROM participante WHERE idParticipante = :id LIMIT 1")
    suspend fun getParticipantePorId(id: String): ParticipanteData?

    @Query("DELETE FROM participante WHERE idParticipante = :id")
    suspend fun eliminar(id: String)

    @Update
    suspend fun actualizar(participante: ParticipanteData)
}