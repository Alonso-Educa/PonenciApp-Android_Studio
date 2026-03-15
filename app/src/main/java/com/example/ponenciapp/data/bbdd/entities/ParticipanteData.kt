package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participante")
data class ParticipanteData(
    @PrimaryKey val idParticipante: String,
    val nombre: String,
    val apellidos: String,
    val emailEduca: String,
    val centro: String,
    val codigoCentro: String,
    val rol: String = "participante", // "participante" | "organizador" | "ponente"
    val fechaRegistro: String = "",
    val idEvento: String = ""
)