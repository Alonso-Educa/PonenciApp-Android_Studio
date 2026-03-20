package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ponenciapp.data.bbdd.entities.MensajeData

@Dao
interface MensajeDao {

    @Query("SELECT * FROM mensaje ORDER BY id ASC")
    suspend fun getMensajes(): List<MensajeData>

    @Insert
    suspend fun insertarMensaje(message: MensajeData)

    @Query("DELETE FROM mensaje")
    suspend fun borrarMensajes()
}
