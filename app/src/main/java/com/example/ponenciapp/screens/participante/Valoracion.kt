package com.example.ponenciapp.screens.participante

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.data.bbdd.entities.PonenciaData
import com.example.ponenciapp.data.bbdd.entities.ValoracionData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.notification.NotificationHandler
import com.example.ponenciapp.screens.comun.BottomBarParticipante
import com.example.ponenciapp.screens.comun.BottomBarUnirseEvento
import com.example.ponenciapp.screens.comun.IconoUsuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Valoracion(navController: NavController) {

    // Variables de estado
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Para las notificaciones
    val notificationHandler = NotificationHandler(context)

    // Base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // Daos de Room
    val valoracionDao = db.valoracionDao()
    val ponenciaDao = db.ponenciaDao()
    val eventoDao = db.eventoDao()
    val participanteDao = db.participanteDao()

    // Variables básicas
    var participante by remember { mutableStateOf<ParticipanteData?>(null) }
    var idEvento by remember { mutableStateOf("") }
    var idParticipante by remember { mutableStateOf("") }

    // Variables de control
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Para la valoración del evento
    var evento by remember { mutableStateOf<EventoData?>(null) }
    var puntuacionEvento by remember { mutableIntStateOf(0) }
    var comentarioEvento by remember { mutableStateOf("") }

    // Para las valoraciones de las ponencias
    var listaPonencias by remember { mutableStateOf<List<PonenciaData>>(emptyList()) }
    var puntuacionesPonencias by remember { mutableStateOf(mapOf<String, Int>()) }

    LaunchedEffect(Unit) {
        // Cargar participante desde Room para obtener idEvento e idParticipante
        participante = participanteDao.getParticipantePorId(uid)
        idEvento = participante?.idEvento ?: ""
        idParticipante = participante?.idParticipante ?: ""

        // Cargar evento desde Firestore
        firestore.collection("eventos").document(idEvento).get()
            .addOnSuccessListener { doc ->
                evento = EventoData(
                    idEvento = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    fecha = doc.getString("fecha") ?: "",
                    lugar = doc.getString("lugar") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    contrasena = doc.getString("contrasena") ?: "",
                    codigoEvento = doc.getString("codigoEvento") ?: ""
                )
                scope.launch { evento?.let { eventoDao.insertar(it) } }
            }
            .addOnFailureListener {
                scope.launch { evento = eventoDao.getEventoPorId(idEvento) }
            }

        // Cargar ponencias desde Firestore
        firestore.collection("ponencias").whereEqualTo("idEvento", idEvento).get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    try {
                        PonenciaData(
                            idPonencia = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            ponente = doc.getString("ponente") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            horaInicio = doc.getString("horaInicio") ?: "",
                            horaFin = doc.getString("horaFin") ?: "",
                            qrCode = doc.getString("qrCode") ?: "",
                            idEvento = idEvento,
                            orden = doc.getLong("orden")?.toInt() ?: 0
                        )
                    } catch (e: Exception) { null }
                }.sortedBy { it.orden }
                scope.launch {
                    ponenciaDao.insertarTodas(lista)
                    listaPonencias = lista
                    isLoading = false
                }
            }
            .addOnFailureListener {
                scope.launch {
                    listaPonencias = ponenciaDao.getPonenciasDeEvento(idEvento)
                    isLoading = false
                    Toast.makeText(context, "Sin conexión, mostrando datos guardados", Toast.LENGTH_SHORT).show()
                }
            }

        // Cargar valoración previa del evento
        firestore.collection("valoraciones")
            .whereEqualTo("idParticipante", idParticipante)
            .whereEqualTo("idEvento", idEvento)
            .whereEqualTo("tipo", "evento")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    puntuacionEvento = doc.getLong("puntuacion")?.toInt() ?: 0
                    comentarioEvento = doc.getString("comentario") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }

        // Cargar valoraciones previas de ponencias
        firestore.collection("valoraciones")
            .whereEqualTo("idParticipante", idParticipante)
            .whereEqualTo("idEvento", idEvento)
            .whereEqualTo("tipo", "ponencia")
            .get()
            .addOnSuccessListener { result ->
                val mapa = result.documents.associate { doc ->
                    (doc.getString("idPonencia") ?: "") to (doc.getLong("puntuacion")?.toInt() ?: 0)
                }
                puntuacionesPonencias = mapa
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Valoración del evento",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    participante?.let { IconoUsuario(participante = it) }
                }
            )
        },
        bottomBar = {
            // Muestra BottomBar de participante
            BottomBarParticipante(
                navController = navController,
                rutaActual = AppScreens.Valoracion.route
            )
        }
    ) { padding ->

        // Si está cargando muestra el iconito de carga
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Valoración del evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Valoración general del evento ${evento?.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "¿Cómo valorarías el evento en general?",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        SelectorEstrellas(
                            puntuacion = puntuacionEvento,
                            onPuntuacionChange = { puntuacionEvento = it })
                        OutlinedTextField(
                            value = comentarioEvento,
                            onValueChange = { comentarioEvento = it },
                            label = { Text("Comentario (opcional)") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    "Valoración de las ponencias (opcional)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(listaPonencias) { ponencia ->
                Card(
                    modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Valoración general de la ponencia ${ponencia.titulo}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "¿Cómo valorarías la ponencia en general?",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        SelectorEstrellas(
                            puntuacion = puntuacionesPonencias[ponencia.idPonencia] ?: 0,
                            onPuntuacionChange = { nuevaPuntuacion ->
                                puntuacionesPonencias = puntuacionesPonencias.toMutableMap().apply {
                                    put(ponencia.idPonencia, nuevaPuntuacion)
                                }
                            }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        // Solo se comprueba la valoración del evento
                        if (puntuacionEvento == 0) {
                            Toast.makeText(
                                context, "Introduce una puntuación del evento", Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        isSaving = true

                        // Guarda la valoración del evento
                        val idValoracion = "${idParticipante}_${idEvento}_evento"
                        firestore.collection("valoraciones").document(idValoracion).set(
                            mapOf(
                                "idValoracion" to idValoracion,
                                "idParticipante" to idParticipante,
                                "idEvento" to idEvento,
                                "idPonencia" to "",
                                "tipo" to "evento",
                                "puntuacion" to puntuacionEvento,
                                "comentario" to comentarioEvento
                            )
                        ).addOnSuccessListener {
                            scope.launch {
                                valoracionDao.insertar(
                                    ValoracionData(
                                        idValoracion = idValoracion,
                                        idParticipante = idParticipante,
                                        idEvento = idEvento,
                                        idPonencia = "",
                                        tipo = "evento",
                                        puntuacion = puntuacionEvento,
                                        comentario = comentarioEvento
                                    )
                                )
                                isSaving = false
                                Toast.makeText(
                                    context, "Valoración enviada correctamente", Toast.LENGTH_SHORT
                                ).show()
                                notificationHandler.enviarNotificacionSimple(
                                    "Valoración enviada correctamente",
                                    "Acabas de enviar una valoración, nuestros organizadores lo agradecerán"
                                )
                            }
                        }.addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(
                                context,
                                "Error guardando valoración: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Guarda la valoración de las ponencias
                        for (ponencia in listaPonencias) {
                            // Solo guarda la valoración de las ponencias que tienen valoración
                            if (puntuacionesPonencias[ponencia.idPonencia] == null) continue
                            val idValoracionPonencia =
                                "${idParticipante}_${idEvento}_${ponencia.idPonencia}_ponencia"
                            firestore.collection("valoraciones").document(idValoracionPonencia).set(
                                mapOf(
                                    "idValoracion" to idValoracionPonencia,
                                    "idParticipante" to idParticipante,
                                    "idEvento" to idEvento,
                                    "idPonencia" to ponencia.idPonencia,
                                    "tipo" to "ponencia",
                                    "puntuacion" to (puntuacionesPonencias[ponencia.idPonencia]
                                        ?: 0),
                                    "comentario" to ""
                                )
                            ).addOnSuccessListener {
                                scope.launch {
                                    valoracionDao.insertar(
                                        ValoracionData(
                                            idValoracion = idValoracionPonencia,
                                            idParticipante = idParticipante,
                                            idEvento = idEvento,
                                            idPonencia = ponencia.idPonencia,
                                            tipo = "ponencia",
                                            puntuacion = (puntuacionesPonencias[ponencia.idPonencia]
                                                ?: 0),
                                            comentario = ""
                                        )
                                    )
                                    isSaving = false
                                    Toast.makeText(
                                        context,
                                        "Valoración guardada correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(
                                    context,
                                    "Error guardando valoración: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp), enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Guardar valoración")
                    }
                }
            }
        }
    }
}

// Selector de estrellas para las valoraciones
@Composable
fun SelectorEstrellas(
    puntuacion: Int, onPuntuacionChange: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { estrella ->
            IconButton(
                // Actualiza el valor de estrella con la puntuación actual
                onClick = { onPuntuacionChange(estrella) }, modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (estrella <= puntuacion) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Estrella $estrella",
                    tint = if (estrella <= puntuacion) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}