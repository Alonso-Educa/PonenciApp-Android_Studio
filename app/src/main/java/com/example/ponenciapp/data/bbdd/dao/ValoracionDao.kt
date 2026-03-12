package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ponenciapp.data.bbdd.entities.ValoracionData

@Dao
interface ValoracionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(valoracion: ValoracionData)

    @Query("SELECT * FROM valoracion WHERE idParticipante = :idParticipante")
    suspend fun getValoracionesDeParticipante(idParticipante: String): List<ValoracionData>

    @Query("SELECT * FROM valoracion WHERE idParticipante = :idParticipante AND tipo = 'evento' LIMIT 1")
    suspend fun getValoracionEvento(idParticipante: String): ValoracionData?
}