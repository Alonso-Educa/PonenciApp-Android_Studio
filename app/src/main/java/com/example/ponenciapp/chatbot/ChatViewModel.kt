package com.example.ponenciapp.chatbot

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room.databaseBuilder
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.MensajeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = databaseBuilder(
        application, AppDB::class.java, Estructura.DB.NAME
    ).fallbackToDestructiveMigration().build()

    private val mensajeDao = db.mensajeDao()
    private val apiKey = "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"
    private val api = Retrofit.Builder().baseUrl("https://api.groq.com/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(GroqApi::class.java)

    var mensajes: SnapshotStateList<Mensaje> = mutableStateListOf()
        private set

    var escribiendo by mutableStateOf(false)
        private set

    var pensando by mutableStateOf(false)
        private set

    private var enviarTarea: Job? = null

    // Índice del mensaje del bot que se está escribiendo actualmente (-1 = ninguno)
    private var botMensajeIndex = -1

    init {
        viewModelScope.launch {
            val mensajesGuardados = withContext(Dispatchers.IO) { mensajeDao.getMensajes() }
            if (mensajesGuardados.isEmpty()) {
                insertarBienvenida()
            } else {
                mensajes.addAll(mensajesGuardados.map { Mensaje(it.rol, it.contenido, it.fecha) })
            }
        }
    }

    fun enviarMensaje(text: String) {
        // Cancela tarea anterior y limpia el mensaje parcial si quedó en la lista
        cancelarTareaEnCurso()

        enviarTarea = viewModelScope.launch {
            // Añadir mensaje del usuario y usar la misma fecha para el objeto y para Room
            val fecha = System.currentTimeMillis()
            val userMessage = Mensaje("user", text, fecha)
            mensajes.add(userMessage)

            withContext(Dispatchers.IO) {
                mensajeDao.insertarMensaje(
                    MensajeData(rol = "user", contenido = text, fecha = fecha)
                )
            }

            try {
                escribiendo = true
                pensando = true
                delay(500)

                // Comprobar si la tarea fue cancelada durante el delay
                if (!isActive) return@launch

                val response = withContext(Dispatchers.IO) {
                    val resultado = api.chat(
                        "Bearer $apiKey",
                        ChatRequest(
                            modelo = "llama-3.1-8b-instant",
                            mensajes = mensajes.map { MensajeApi(it.rol, it.contenido) }
                        )
                    )
                    if (!resultado.isSuccessful) {
                        Log.e(
                            "GROQ_ERROR",
                            resultado.errorBody()?.string() ?: "Sin mensaje de error"
                        )
                    }
                    resultado
                }

                pensando = false

                if (response.isSuccessful) {
                    val respuesta =
                        response.body()?.choices?.firstOrNull()?.mensajes?.contenido ?: ""
                    val botFecha = System.currentTimeMillis()

                    // Añadir mensaje vacío y guardar su índice directamente
                    val botMessage = Mensaje("assistant", "", botFecha)
                    mensajes.add(botMessage)
                    botMensajeIndex = mensajes.lastIndex

                    // usar el índice rastreado en lugar de indexOf()
                    for (char in respuesta) {
                        if (!isActive) break // dejar de escribir si se cancela
                        delay(5)
                        val idx = botMensajeIndex
                        if (idx != -1 && idx < mensajes.size) {
                            mensajes[idx] = mensajes[idx].copy(
                                contenido = mensajes[idx].contenido + char
                            )
                        }
                    }

                    // Guardar en Room solo el contenido final completo
                    val contenidoFinal =
                        if (botMensajeIndex != -1) mensajes[botMensajeIndex].contenido else respuesta
                    withContext(Dispatchers.IO) {
                        mensajeDao.insertarMensaje(
                            MensajeData(rol = "assistant", contenido = contenidoFinal, fecha = botFecha)
                        )
                    }

                } else {
                    mensajes.add(
                        Mensaje(
                            "assistant",
                            "¡Error! Código: ${response.code()}",
                            System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                // No mostrar error si fue una cancelación intencionada
                if (isActive) {
                    mensajes.add(
                        Mensaje(
                            "assistant",
                            "¡Error! ${e.message}",
                            System.currentTimeMillis()
                        )
                    )
                }
            } finally {
                // resetear ambos estados siempre
                escribiendo = false
                pensando = false
                botMensajeIndex = -1
            }
        }
    }

    fun borrarMensajes() {
        cancelarTareaEnCurso()

        viewModelScope.launch {
            withContext(Dispatchers.IO) { mensajeDao.borrarMensajes() }
            mensajes.clear()
            insertarBienvenida()
        }
    }

    private fun cancelarTareaEnCurso() {
        enviarTarea?.cancel()
        enviarTarea = null
        escribiendo = false
        pensando = false

        // eliminar el mensaje en curso del bot si quedó en la lista
        if (botMensajeIndex != -1 && botMensajeIndex < mensajes.size) {
            mensajes.removeAt(botMensajeIndex)
        }
        botMensajeIndex = -1
    }

    private suspend fun insertarBienvenida() {
        val bienvenida = MensajeData(
            rol = "assistant",
            contenido = "Hola! Soy Groq, tu asistente virtual. Escríbeme si necesitas algo, estaré encantado de ayudar.",
            fecha = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) { mensajeDao.insertarMensaje(bienvenida) }
        mensajes.add(Mensaje(bienvenida.rol, bienvenida.contenido, bienvenida.fecha))
    }
}