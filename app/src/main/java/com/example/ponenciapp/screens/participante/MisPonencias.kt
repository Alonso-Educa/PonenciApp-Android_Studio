package com.example.ponenciapp.screens.participante

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.comun.BottomBarParticipante
import com.example.ponenciapp.screens.utilidad.IconoUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPonencias(navController: NavController) {

    // variables de estado
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val participanteDao = db.participanteDao()
    val ponenciaDao = db.ponenciaDao()
    val eventoDao = db.eventoDao()

    var participante by remember { mutableStateOf<ParticipanteData?>(null) }
    var listaPonencias by remember { mutableStateOf<List<PonenciaData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var evento by remember { mutableStateOf<EventoData?>(null) }

    LaunchedEffect(Unit) {
        // Carga el participante desde Room
        participante = participanteDao.getParticipantePorId(uid)
        val idEvento = participante?.idEvento ?: ""
        // Carga el evento
        firestore.collection("eventos").document(idEvento).get().addOnSuccessListener { doc ->
            evento = EventoData(
                idEvento = doc.id,
                nombre = doc.getString("nombre") ?: "",
                fecha = doc.getString("fecha") ?: "",
                lugar = doc.getString("lugar") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                codigoEvento = doc.getString("codigoEvento") ?: "",
                idOrganizador = doc.getString("idOrganizador") ?: ""
            )
            scope.launch { eventoDao.insertar(evento!!) }
        }
            // Si firebase falla se cargan los datos desde room
            .addOnFailureListener {
                scope.launch { evento = eventoDao.getEventoPorId(idEvento) }
            }

        // Carga las ponencias
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
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.orden }

                scope.launch {
                    ponenciaDao.insertarTodas(lista)
                    listaPonencias = lista
                    isLoading = false
                }
            }
            // Si firebase falla se cargan los datos desde room
            .addOnFailureListener {
                scope.launch {
                    val listaLocal = ponenciaDao.getPonenciasDeEvento(idEvento)
                    listaPonencias = listaLocal
                    if (listaLocal.isEmpty()) {
                        Toast.makeText(
                            context, "Sin conexión y sin datos guardados", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context, "Sin conexión, mostrando datos guardados", Toast.LENGTH_SHORT
                        ).show()
                    }
                    isLoading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Mis Ponencias",
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
                    participante?.let { IconoUsuario(
                        usuario = it
                    ) }
                }
            )
        },
        bottomBar = {
            BottomBarParticipante(
                navController = navController,
                rutaActual = AppScreens.Ponencias.route
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

        // Si no hay ponencias muestra un mensaje
        if (listaPonencias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay ponencias disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        // Sino, muestra la lista de ponencias
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            // Tarjeta con la información del evento
            evento?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            it.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Fecha",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                it.fecha, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Lugar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                it.lugar, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (it.descripcion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                it.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Lista de ponencias
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaPonencias) { ponencia ->
                    TarjetaPonencia(
                        ponencia = ponencia, onClick = {
                            navController.navigate(
                                AppScreens.DetallePonenciaParticipante.createRoute(ponencia.idPonencia)
                            )
                        }
                    )
                }
            }
        }
    }
}

// Tarjeta de ponencia
@Composable
fun TarjetaPonencia(
    ponencia: PonenciaData, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de orden
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary, CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    "${ponencia.orden}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ponencia.titulo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    ponencia.ponente,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${ponencia.horaInicio} - ${ponencia.horaFin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray
            )
        }
    }
}