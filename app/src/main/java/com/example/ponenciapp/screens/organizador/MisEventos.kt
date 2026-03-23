package com.example.ponenciapp.screens.organizador

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Lucide
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.comun.BottomBarOrganizador
import com.example.ponenciapp.screens.comun.BottomBarUnirseEvento
import com.example.ponenciapp.screens.comun.IconoUsuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEventos(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = Firebase.auth.currentUser?.uid ?: ""

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val eventoDao = db.eventoDao()
    val participanteDao = db.participanteDao()

    var participante by remember { mutableStateOf<ParticipanteData?>(null) }

    var listaEventos by remember { mutableStateOf<List<EventoData>>(emptyList()) }
    var showDialogCrear by remember { mutableStateOf(false) }
    var showDialogEliminar by remember { mutableStateOf(false) }
    var eventoEditando by remember { mutableStateOf<EventoData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar eventos del organizador
    LaunchedEffect(Unit) {
        // Carga el participante desde Room
        participante = participanteDao.getParticipantePorId(uid)

        // Carga los eventos del organizador
        firestore.collection("eventos").whereEqualTo("idOrganizador", uid).get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    try {
                        EventoData(
                            idEvento = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            fecha = doc.getString("fecha") ?: "",
                            lugar = doc.getString("lugar") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            contrasena = doc.getString("contrasena") ?: "",
                            codigoEvento = doc.getString("codigoEvento") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                scope.launch {
                    lista.forEach { eventoDao.insertar(it) }
                    listaEventos = lista
                    isLoading = false
                }
            }.addOnFailureListener {
                scope.launch {
                    listaEventos = eventoDao.getTodosEventos()
                    if (listaEventos.isEmpty()) {
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
                            "Mis Eventos",
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
            // Muestra BottomBar de organizador
            BottomBarOrganizador(
                navController = navController,
                rutaActual = AppScreens.MisEventos.route
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

        // Contenido de la pantalla
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Si no hay eventos, mostrar mensaje de ayuda
            if (listaEventos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = "Evento",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tienes eventos creados",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pulsa el botón + para crear uno",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                // Si hay eventos, mostrarlos en una lista
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    items(listaEventos) { evento ->
                        // Tarjeta de evento
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    navController.navigate(
                                        AppScreens.DetalleEvento.createRoute(evento.idEvento)
                                    )
                                }, elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        evento.nombre,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Lucide.CalendarDays,
                                            contentDescription = "Fecha de inicio",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            " ${evento.fecha}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Lugar de localización",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            " ${evento.lugar}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        "Código: ${evento.codigoEvento}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                // Editar / Eliminar
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = {
                                        eventoEditando = evento
                                        showDialogCrear = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = {
                                        eventoEditando = evento
                                        showDialogEliminar = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB para crear un nuevo evento
            FloatingActionButton(
                onClick = {
                    eventoEditando = null
                    showDialogCrear = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear evento", tint = Color.White)
            }
        }

        // Dialog para crear o editar evento
        if (showDialogCrear) {
            DialogCrearEvento(eventoEditando = eventoEditando, idOrganizador = uid, onDismiss = {
                showDialogCrear = false
                eventoEditando = null
            }, onGuardado = { eventoNuevo ->
                listaEventos = if (eventoEditando == null) {
                    listaEventos + eventoNuevo
                } else {
                    listaEventos.map { if (it.idEvento == eventoNuevo.idEvento) eventoNuevo else it }
                }
                showDialogCrear = false
                eventoEditando = null
            })
        }

        // Dialog para confirmar la eliminación del evento
        if (showDialogEliminar && eventoEditando != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialogEliminar = false
                    eventoEditando = null
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                title = { Text("Eliminar evento") },
                text = { Text("¿Deseas eliminar el evento \"${eventoEditando!!.nombre}\"? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val id = eventoEditando!!.idEvento
                            eventoDao.eliminar(id)
                            firestore.collection("eventos").document(id).delete()
                            listaEventos = listaEventos.filter { it.idEvento != id }
                            showDialogEliminar = false
                            eventoEditando = null
                            Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialogEliminar = false
                        eventoEditando = null
                    }) { Text("Cancelar") }
                })
        }
    }
}

// Función usada para crear/editar un nuevo evento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCrearEvento(
    eventoEditando: EventoData?,
    idOrganizador: String,
    onDismiss: () -> Unit,
    onGuardado: (EventoData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val eventoDao = db.eventoDao()

    // Atributos del evento
    var nombre by remember { mutableStateOf(eventoEditando?.nombre ?: "") }
    var fecha by remember { mutableStateOf(eventoEditando?.fecha ?: "") }
    var lugar by remember { mutableStateOf(eventoEditando?.lugar ?: "") }
    var descripcion by remember { mutableStateOf(eventoEditando?.descripcion ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (eventoEditando == null) "Crear evento" else "Editar evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Nombre del evento
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del evento") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()

                // Fecha del evento
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    label = { Text("Fecha del evento") },
                    placeholder = { Text("dd/MM/yyyy") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    },
                    readOnly = true,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true })

                // Mostrar el date picker para la fecha
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    fecha = convertMillisToDate(it)
                                }
                                showDatePicker = false
                            }) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Lugar del evento
                OutlinedTextField(
                    value = lugar,
                    onValueChange = { lugar = it },
                    label = { Text("Lugar") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción del evento
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // acciones
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }

                    // Botón de aceptar
                    Button(
                        onClick = {
                            when {
                                // Comprobaciones
                                nombre.isBlank() -> Toast.makeText(
                                    context, "Introduce el nombre", Toast.LENGTH_SHORT
                                ).show()

                                fecha.isBlank() -> Toast.makeText(
                                    context, "Introduce la fecha", Toast.LENGTH_SHORT
                                ).show()

                                lugar.isBlank() -> Toast.makeText(
                                    context, "Introduce el lugar", Toast.LENGTH_SHORT
                                ).show()

                                else -> {
                                    isLoading = true
                                    if (eventoEditando == null) {
                                        // Crea un nuevo evento
                                        val idEvento = UUID.randomUUID().toString()
                                        val codigo = generarCodigoEvento()
                                        val data = mapOf(
                                            "nombre" to nombre,
                                            "fecha" to fecha,
                                            "lugar" to lugar,
                                            "descripcion" to descripcion,
                                            "codigoEvento" to codigo,
                                            "contrasena" to "",
                                            "idOrganizador" to idOrganizador
                                        )
                                        // Guarda el evento en Firestore
                                        firestore.collection("eventos").document(idEvento).set(data)
                                            .addOnSuccessListener {
                                                val eventoNuevo = EventoData(
                                                    idEvento = idEvento,
                                                    nombre = nombre,
                                                    fecha = fecha,
                                                    lugar = lugar,
                                                    descripcion = descripcion,
                                                    contrasena = "",
                                                    codigoEvento = codigo
                                                )
                                                scope.launch {
                                                    eventoDao.insertar(eventoNuevo)
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Evento creado. Código: $codigo",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    onGuardado(eventoNuevo)
                                                }
                                            }.addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        // Edita el evento existente
                                        val data = mapOf(
                                            "nombre" to nombre,
                                            "fecha" to fecha,
                                            "lugar" to lugar,
                                            "descripcion" to descripcion
                                        )
                                        firestore.collection("eventos")
                                            .document(eventoEditando.idEvento).update(data)
                                            .addOnSuccessListener {
                                                val eventoActualizado = eventoEditando.copy(
                                                    nombre = nombre,
                                                    fecha = fecha,
                                                    lugar = lugar,
                                                    descripcion = descripcion
                                                )
                                                scope.launch {
                                                    eventoDao.insertar(eventoActualizado)
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Evento actualizado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    onGuardado(eventoActualizado)
                                                }
                                            }.addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }
                        }, enabled = !isLoading
                    ) {
                        // Mientras que carga, se muestra el iconito de carga
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White, modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(if (eventoEditando == null) "Crear" else "Guardar")
                        }
                    }
                }
            }
        }
    }
}

// Para generar un código con patrón tipo "FORM-X7K2"
fun generarCodigoEvento(): String {
    // Tiene las letras y números y genera un string de 4 caracteres aleatorios
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val codigoAleatorio = (1..4).map { chars.random() }.joinToString("")
    return "FORM-$codigoAleatorio"
}

// Formato de la fecha normal
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}