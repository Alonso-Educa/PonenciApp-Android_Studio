package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ponenciapp.data.bbdd.entities.PonenciaData

@Dao
interface PonenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ponencia: PonenciaData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(ponencias: List<PonenciaData>)

    @Query("SELECT * FROM ponencia WHERE idEvento = :idEvento ORDER BY horaInicio ASC")
    suspend fun getPonenciasDeEvento(idEvento: String): List<PonenciaData>

    @Query("SELECT * FROM ponencia WHERE idPonencia = :id LIMIT 1")
    suspend fun getPonenciaPorId(id: String): PonenciaData?

    @Query("DELETE FROM ponencia WHERE idEvento = :idEvento")
    suspend fun eliminarPonenciasDeEvento(idEvento: String)

    @Query("DELETE FROM ponencia WHERE idPonencia = :id")
    suspend fun eliminarPonencia(id: String)

}