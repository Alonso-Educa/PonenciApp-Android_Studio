package com.example.ponenciapp.data.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ponenciapp.data.bbdd.entities.UsuarioData

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioData)

    @Query("SELECT * FROM usuario WHERE idUsuario = :id LIMIT 1")
    suspend fun getParticipantePorId(id: String): UsuarioData?

    @Query("DELETE FROM usuario WHERE idUsuario = :id")
    suspend fun eliminar(id: String)

    @Update
    suspend fun actualizar(usuario: UsuarioData)
}