package com.example.ponenciapp.screens.organizador

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.PonenciaData
import com.example.ponenciapp.data.bbdd.entities.UsuarioData
import com.example.ponenciapp.screens.organizador.DialogMostrarQR
import com.example.ponenciapp.screens.utilidad.IconoUsuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePonenciaOrganizador(navController: NavController, idPonencia: String) {

    // variables de estado
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = Firebase.auth.currentUser?.uid ?: ""
    val auth = Firebase.auth

    // base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // daos de room
    val ponenciaDao = db.ponenciaDao()
    val usuarioDao = db.usuarioDao()

    // variables de estado
    var ponencia by remember { mutableStateOf<PonenciaData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var participante by remember { mutableStateOf<UsuarioData?>(null) }
    var evento by remember { mutableStateOf<EventoData?>(null) }
    var showQREvento by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // carga el participante desde room
        participante = usuarioDao.getParticipantePorId(uid)

        // carga la ponencia desde firebase
        firestore.collection("ponencias").document(idPonencia).get()
            .addOnSuccessListener { doc ->
                // Añadir dentro del addOnSuccessListener de la ponencia
                val idEvento = doc.getString("idEvento") ?: ""
                if (idEvento.isNotEmpty()) {
                    firestore.collection("eventos").document(idEvento).get()
                        .addOnSuccessListener { eventoDoc ->
                            evento = EventoData(
                                idEvento = eventoDoc.id,
                                nombre = eventoDoc.getString("nombre") ?: "",
                                fecha = eventoDoc.getString("fecha") ?: "",
                                lugar = eventoDoc.getString("lugar") ?: "",
                                descripcion = eventoDoc.getString("descripcion") ?: "",
                                codigoEvento = eventoDoc.getString("codigoEvento") ?: "",
                                idOrganizador = eventoDoc.getString("idOrganizador") ?: ""
                            )
                        }
                }
                ponencia = PonenciaData(
                    idPonencia = doc.id,
                    titulo = doc.getString("titulo") ?: "",
                    ponente = doc.getString("ponente") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    horaInicio = doc.getString("horaInicio") ?: "",
                    horaFin = doc.getString("horaFin") ?: "",
                    qrCode = doc.getString("qrCode") ?: "",
                    idEvento = doc.getString("idEvento") ?: "",
                    orden = doc.getLong("orden")?.toInt() ?: 0
                )
                scope.launch {
                    ponenciaDao.insertar(ponencia!!)
                    isLoading = false
                }
            }.addOnFailureListener {
                scope.launch {
                    ponencia = ponenciaDao.getPonenciaPorId(idPonencia)
                    isLoading = false
                    Toast.makeText(
                        context, "Sin conexión, mostrando datos guardados", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ponencia?.titulo ?: "Información de la ponencia",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                        //modifier = Modifier.padding(end = 48.dp)
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ), navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                }, actions = {
                    // Icono de usuario
                    participante?.let {
                        IconoUsuario(
                            usuario = it
                        )
                    }
                })
        }) { padding ->

        // Si está cargando muestra el iconito de carga
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Muestra la información de la ponencia
        ponencia?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera con número de orden
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Ponencia ${p.orden}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Título
                Text(
                    p.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Ponente
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        p.ponente.ifBlank { " - " },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                // Horario de la ponencia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Horario",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${p.horaInicio} - ${p.horaFin}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Fecha",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    evento?.fecha ?: "", style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Lugar",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    evento?.lugar ?: "", style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Descripción
                if (p.descripcion.isNotBlank()) {
                    Text(
                        "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        p.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Botón para generar el QR del evento
                Button(
                    onClick = { showQREvento = true }, modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver QR de Check-in")
                }
            }
        }
    }

    if (showQREvento) {
        evento?.let {
            DialogMostrarQR(
                titulo = "QR Check-in — ${it.nombre} — ${ponencia?.titulo}",
                contenidoQR = "checkin:${it.idEvento}:${ponencia?.idPonencia}",
                onDismiss = { showQREvento = false }
            )
        }
    }
}