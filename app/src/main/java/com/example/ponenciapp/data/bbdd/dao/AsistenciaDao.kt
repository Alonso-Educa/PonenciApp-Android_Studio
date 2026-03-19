package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ponenciapp.data.bbdd.entities.AsistenciaData

@Dao
interface AsistenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(asistencia: AsistenciaData)

    @Query("SELECT * FROM asistencia WHERE idParticipante = :idParticipante")
    suspend fun getAsistenciasDeParticipante(idParticipante: String): List<AsistenciaData>

    @Query("SELECT * FROM asistencia WHERE idParticipante = :idParticipante AND tipo = 'checkin' LIMIT 1")
    suspend fun getCheckIn(idParticipante: String): AsistenciaData?

    @Query("SELECT * FROM asistencia WHERE idParticipante = :idParticipante AND idPonencia = :idPonencia LIMIT 1")
    suspend fun getAsistenciaAPonencia(idParticipante: String, idPonencia: String): AsistenciaData?

    @Query("SELECT * FROM asistencia WHERE idEvento = :idEvento")
    suspend fun getAsistenciasDeEvento(idEvento: String): List<AsistenciaData>
}