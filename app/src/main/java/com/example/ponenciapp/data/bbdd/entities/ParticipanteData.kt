package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioData(
    @PrimaryKey val idUsuario: String,
    val nombre: String,
    val apellidos: String,
    val emailEduca: String,
    val centro: String,
    val codigoCentro: String,
    val rol: String = "participante", // "participante" | "organizador"
    val fechaRegistro: String = "",
    val idEvento: String = "",
    val fotoPerfilUrl: String? = ""
)