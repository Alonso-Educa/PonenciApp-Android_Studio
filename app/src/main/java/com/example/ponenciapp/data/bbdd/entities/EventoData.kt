package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evento")
data class EventoData(
    @PrimaryKey val idEvento: String,
    val nombre: String,
    val fecha: String,
    val lugar: String,
    val descripcion: String,
    val codigoEvento: String,
    val idOrganizador: String
)
