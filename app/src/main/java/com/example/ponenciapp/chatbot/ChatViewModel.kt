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
import com.example.ponenciapp.BuildConfig
import com.example.ponenciapp.notification.NotificationHandler
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // ─── Companion object: constantes de configuración ───────────────────────
    companion object {
        private const val MAX_HISTORIAL = 20

        private const val SYSTEM_PROMPT = """
Eres el asistente virtual de PonenciApp, una aplicación para la gestión de eventos y ponencias educativas. Tu nombre es "Asistente PonenciApp".

SOBRE LA APP:
- PonenciApp tiene dos roles: Organizador y Participante.
- Organizadores: crean eventos (nombre, fecha, lugar, descripción), añaden ponencias con horarios, generan códigos QR para check-in, registran participantes manualmente y consultan estadísticas. Pueden usar el panel web (Flutter) o la app móvil Android.
- Participantes: se unen a eventos con un código (ej. FORM-X7K2), hacen check-in escaneando un QR, consultan ponencias, valoran eventos y ponencias con estrellas y comentarios. Solo usan la app móvil Android.

FUNCIONALIDADES CLAVE:
- Login: email/contraseña, Google o Microsoft (Educacyl). Se pueden vincular varios proveedores en Ajustes.
- Eventos: cada evento tiene un código único autogenerado. El organizador comparte este código con los participantes.
- Ponencias: se añaden dentro de un evento con título, ponente, descripción, hora inicio y fin.
- Check-in QR: el organizador genera un QR para el evento. Los participantes lo escanean con la cámara para registrar asistencia.
- Valoraciones: los participantes puntúan de 1 a 5 estrellas tanto el evento como cada ponencia, con comentario opcional.
- Estadísticas: el organizador ve resúmenes (total eventos, ponencias, próximo evento, participantes). Puede exportar asistencias en PDF o Excel desde la app móvil.
- Perfil: editar nombre, apellidos, email educativo, centro, código de centro, foto de perfil.
- Seguridad: cambiar email o contraseña requiere re-autenticación. Se puede eliminar la cuenta completamente desde Ajustes.
- Modo oscuro: disponible en Ajustes.
- Base de datos local: la app usa Room para caché offline de datos del usuario, eventos y ponencias.

INSTRUCCIONES DE COMPORTAMIENTO:
- Responde siempre en español.
- Sé conciso, amable y útil.
- Si te preguntan algo que no tiene que ver con PonenciApp, puedes responder brevemente pero redirige la conversación hacia la app.
- Si no sabes algo específico de la app, dilo honestamente y sugiere contactar con soporte.
- Guía al usuario paso a paso cuando pregunte cómo hacer algo.
- No inventes funcionalidades que no existen.
"""
    }
    // ─────────────────────────────────────────────────────────────────────────

    private val notificationHandler = NotificationHandler(application.applicationContext)
    var usuarioEnChat by mutableStateOf(false)

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // Referencia dinámica: siempre apunta al usuario actual en el momento de usarse
    private val mensajesRef
        get() = firestore
            .collection("chats")
            .document(auth.currentUser?.uid ?: "anonimo")
            .collection("mensajes")

    private val apiKey = BuildConfig.GROQ_API_KEY
    private val api = Retrofit.Builder()
        .baseUrl("https://api.groq.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GroqApi::class.java)

    var mensajes: SnapshotStateList<Mensaje> = mutableStateListOf()
        private set

    var escribiendo by mutableStateOf(false)
        private set

    var pensando by mutableStateOf(false)
        private set

    var rolUsuario: String = "participante"
        private set

    private var guardado by mutableStateOf(false)
    private var enviarTarea: Job? = null
    private var botMensajeIndex = -1
    private var cargandoMensajes = false

    init {
        cargarRolUsuario()
        recargarMensajes()
    }

    /**
     * Limpia la lista local y carga los mensajes del usuario actual desde Firestore.
     * Llamar también al entrar a la pantalla por si el usuario cambió de cuenta.
     */
    fun recargarMensajes() {
        if (cargandoMensajes) return

        viewModelScope.launch {
            cargandoMensajes = true
            mensajes.clear()

            try {
                val snapshot = withContext(Dispatchers.IO) {
                    mensajesRef
                        .orderBy("fecha", Query.Direction.ASCENDING)
                        .get()
                        .await()
                }

                val cargados = snapshot.documents.mapNotNull { doc ->
                    val rol = doc.getString("rol") ?: return@mapNotNull null
                    val contenido = doc.getString("contenido") ?: return@mapNotNull null
                    val fecha = doc.getLong("fecha") ?: return@mapNotNull null
                    Mensaje(rol, contenido, fecha)
                }

                if (cargados.isEmpty()) {
                    insertarBienvenida()
                } else {
                    mensajes.addAll(cargados)
                }

            } catch (e: Exception) {
                Log.e("FIRESTORE", "Error cargando mensajes: ${e.message}")
                insertarBienvenida()
            } finally {
                cargandoMensajes = false
            }
        }
    }

    private fun cargarRolUsuario() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = withContext(Dispatchers.IO) {
                    firestore.collection("usuarios").document(uid).get().await()
                }
                rolUsuario = doc.getString("rol") ?: "participante"
            } catch (e: Exception) {
                Log.e("FIRESTORE", "Error cargando rol: ${e.message}")
            }
        }
    }

    fun enviarMensaje(text: String) {
        cancelarTareaEnCurso()

        enviarTarea = viewModelScope.launch {
            val fecha = System.currentTimeMillis()
            mensajes.add(Mensaje("user", text, fecha))
            guardarEnFirestore("user", text, fecha)

            try {
                guardado = false
                escribiendo = true
                pensando = true
                delay(500)

                if (!isActive) return@launch

                // ── Contexto dinámico del usuario ────────────────────────────
                // Se pueden enviar más cosas acerca del usuario para mejorar su experiencia TODO()
                val contextoUsuario = "El usuario actual tiene el rol de $rolUsuario en la app."
                val systemConContexto = SYSTEM_PROMPT.trimIndent() + "\n\n$contextoUsuario"

                // ── Limitar historial a los últimos MAX_HISTORIAL mensajes ───
                val historialLimitado = mensajes
                    .takeLast(MAX_HISTORIAL)
                    .map { MensajeApi(it.rol, it.contenido) }

                val response = withContext(Dispatchers.IO) {
                    val resultado = api.chat(
                        "Bearer $apiKey",
                        ChatRequest(
                            modelo = "llama-3.1-8b-instant",
                            mensajes = listOf(MensajeApi("system", systemConContexto)) +
                                    historialLimitado
                        )
                    )
                    if (!resultado.isSuccessful)
                        Log.e("GROQ_ERROR", resultado.errorBody()?.string() ?: "Sin mensaje de error")
                    resultado
                }

                pensando = false

                if (response.isSuccessful) {
                    val respuesta = response.body()?.choices?.firstOrNull()?.mensajes?.contenido ?: ""
                    val botFecha = System.currentTimeMillis()

                    mensajes.add(Mensaje("assistant", "", botFecha))
                    botMensajeIndex = mensajes.lastIndex

                    for (char in respuesta) {
                        if (!isActive) break
                        delay(5)
                        val idx = botMensajeIndex
                        if (idx != -1 && idx < mensajes.size) {
                            mensajes[idx] = mensajes[idx].copy(contenido = mensajes[idx].contenido + char)
                        }
                    }

                    val contenidoFinal =
                        if (botMensajeIndex != -1) mensajes[botMensajeIndex].contenido else respuesta
                    guardarEnFirestore("assistant", contenidoFinal, botFecha)
                    guardado = true

                } else {
                    mensajes.add(Mensaje("assistant", "¡Error! Código: ${response.code()}", System.currentTimeMillis()))
                }

            } catch (e: Exception) {
                if (isActive)
                    mensajes.add(Mensaje("assistant", "¡Error! ${e.message}", System.currentTimeMillis()))
            } finally {
                // Guardar mensaje parcial si se interrumpió antes de terminar
                if (!guardado && botMensajeIndex != -1 && botMensajeIndex < mensajes.size) {
                    val parcial = mensajes[botMensajeIndex].contenido
                    if (parcial.isNotBlank())
                        guardarEnFirestore("assistant", parcial, System.currentTimeMillis())
                }

                if (!usuarioEnChat) {
                    notificationHandler.enviarNotificacionConDestino(
                        titulo = "Tu consulta ha sido respondida",
                        cuerpo = "El asistente ha terminado de responder",
                        destino = "ChatbotAsistente"
                    )
                }

                escribiendo = false
                pensando = false
                botMensajeIndex = -1
            }
        }
    }

    fun borrarMensajes() {
        cancelarTareaEnCurso()

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val docs = mensajesRef.get().await()
                    // Firestore no borra subcolecciones en cascada: hay que borrar doc a doc
                    val batch = firestore.batch()
                    docs.documents.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                Log.e("FIRESTORE", "Error borrando mensajes: ${e.message}")
            }
            mensajes.clear()
            insertarBienvenida()
        }
    }

    private suspend fun guardarEnFirestore(rol: String, contenido: String, fecha: Long) {
        try {
            withContext(Dispatchers.IO) {
                mensajesRef.add(
                    hashMapOf("rol" to rol, "contenido" to contenido, "fecha" to fecha)
                ).await()
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE", "Error guardando mensaje ($rol): ${e.message}")
        }
    }

    private suspend fun insertarBienvenida() {
        // ── Mensaje de bienvenida actualizado ────────────────────────────────
        val contenido = "¡Hola! Soy el asistente de PonenciApp. Puedo ayudarte con cualquier duda sobre la app: cómo crear eventos, unirte como participante, hacer check-in con QR, valorar ponencias y mucho más. ¿En qué puedo ayudarte?"
        val fecha = System.currentTimeMillis()

        try {
            withContext(Dispatchers.IO) {
                val docRef = mensajesRef.document("bienvenida")
                val existente = docRef.get().await()

                if (!existente.exists()) {
                    docRef.set(
                        hashMapOf(
                            "rol" to "assistant",
                            "contenido" to contenido,
                            "fecha" to fecha
                        )
                    ).await()
                }
            }

            if (mensajes.none { it.contenido == contenido }) {
                mensajes.add(Mensaje("assistant", contenido, fecha))
            }

        } catch (e: Exception) {
            Log.e("FIRESTORE", "Error insertando bienvenida: ${e.message}")
        }
    }

    private fun cancelarTareaEnCurso() {
        enviarTarea?.cancel()
        enviarTarea = null
        escribiendo = false
        pensando = false
        botMensajeIndex = -1
    }
}