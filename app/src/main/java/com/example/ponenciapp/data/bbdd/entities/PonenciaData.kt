package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ponencia")
data class PonenciaData(
    @PrimaryKey val idPonencia: String,
    val titulo: String,
    val ponente: String,
    val descripcion: String,
    val horaInicio: String,
    val horaFin: String,
    val qrCode: String = "",
    val idEvento: String
)