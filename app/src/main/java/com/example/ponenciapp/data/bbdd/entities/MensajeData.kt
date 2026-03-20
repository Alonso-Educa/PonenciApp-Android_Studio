package com.example.ponenciapp.data.bbdd.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensaje")
data class MensajeData(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val rol: String,
    val contenido: String,
    val fecha: Long
)
