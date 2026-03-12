package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencia")
data class AsistenciaData(
    @PrimaryKey val idAsistencia: String,
    val idParticipante: String,
    val idPonencia: String = "", // vacío si tipo = "checkin"
    val tipo: String,            // "checkin" | "ponencia"
    val fechaHora: String
)