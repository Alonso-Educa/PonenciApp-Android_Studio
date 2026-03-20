package com.example.ponenciapp.chatbot

import com.google.gson.annotations.SerializedName

// Clases de datos para el chatbot

// Para la UI de mensajes
data class Mensaje(
    val rol: String, var contenido: String, val fecha: Long
)

// Solo para enviar a la API
data class MensajeApi(
    @SerializedName("role") val rol: String, @SerializedName("content") val contenido: String
)

// Request usa MensajeApi, no Mensaje
data class ChatRequest(
    @SerializedName("model") val modelo: String,
    @SerializedName("messages") val mensajes: List<MensajeApi>
)

// Response también usa MensajeApi (la fecha de mensaje no viene de la API)
data class Choice(
    @SerializedName("message") val mensajes: MensajeApi
)

data class ChatResponse(val choices: List<Choice>)








