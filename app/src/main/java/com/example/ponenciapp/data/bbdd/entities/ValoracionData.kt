package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "valoracion")
data class ValoracionData(
    @PrimaryKey val idValoracion: String,
    val idParticipante: String,
    val idEvento: String,
    val idPonencia: String = "",
    val tipo: String,   // "evento" | "ponencia"
    val puntuacion: Int,
    val comentario: String = ""
)