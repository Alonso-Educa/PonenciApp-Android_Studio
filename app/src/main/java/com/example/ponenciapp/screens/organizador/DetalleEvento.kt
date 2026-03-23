package com.example.ponenciapp.screens.organizador

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.File
import com.composables.icons.lucide.FileSpreadsheet
import com.composables.icons.lucide.Lucide
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.data.bbdd.entities.PonenciaData
import com.example.ponenciapp.data.generarQRBitmap
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.comun.exportarAsistenciasExcel
import com.example.ponenciapp.screens.comun.exportarAsistenciasPdf
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

// Para mostrar los detalles del evento cuando lo tocas
@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEvento(navController: NavController, idEvento: String) {

    // Variables usadas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = Firebase.auth
    val uid = Firebase.auth.currentUser?.uid ?: ""

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // Dao
    val eventoDao = db.eventoDao()
    val ponenciaDao = db.ponenciaDao()
    val participanteDao = db.participanteDao()

    // Variables de estado
    var organizador by remember { mutableStateOf<ParticipanteData?>(null) }
    var evento by remember { mutableStateOf<EventoData?>(null) }
    var listaPonencias by remember { mutableStateOf<List<PonenciaData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialogPonencia by remember { mutableStateOf(false) }
    var showDialogEliminar by remember { mutableStateOf(false) }
    var ponenciaEditando by remember { mutableStateOf<PonenciaData?>(null) }
    var showQREvento by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Carga el usuario creador del evento
        organizador = participanteDao.getParticipantePorId(uid)
        // Carga el evento desde firebase
        firestore.collection("eventos").document(idEvento).get().addOnSuccessListener { doc ->
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
            }.addOnFailureListener {
                scope.launch {
                    evento = eventoDao.getEventoPorId(idEvento)
                }
            }

        // Carga sus ponencias desde firebase
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
            }.addOnFailureListener {
                scope.launch {
                    listaPonencias = ponenciaDao.getPonenciasDeEvento(idEvento)
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
                    text = evento?.nombre ?: "Detalle del evento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White
            ), navigationIcon = {
                // Botón para volver atrás
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
            }, actions = {
                // Icono de usuario
                organizador?.let { usuario ->
                    var showCardDialog by remember { mutableStateOf(false) }
                    val inicial = usuario.nombre.firstOrNull()?.uppercase() ?: "U"

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                            .clickable { showCardDialog = true }) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = inicial,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    // Dialog con los datos del participante
                    if (showCardDialog) {
                        Dialog(onDismissRequest = { showCardDialog = false }) {
                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(8.dp),
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .widthIn(min = 200.dp, max = 300.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.tertiary,
                                                    CircleShape
                                                )
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.secondary,
                                                    CircleShape
                                                )
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.Center),
                                                text = inicial,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White
                                            )
                                        }
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "${usuario.nombre} ${usuario.apellidos}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Email: ${usuario.emailEduca}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "Centro: ${usuario.centro} - ${usuario.codigoCentro}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "Rol: ${usuario.rol}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    Button(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        onClick = { showCardDialog = false }) {
                                        Text("Cerrar")
                                    }
                                }
                            }
                        }
                    }
                }
            })
        },
        // El fab se usa para añadir ponencias
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ponenciaEditando = null
                    showDialogPonencia = true
                }, containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir ponencia", tint = Color.White)
            }
        }
    ) { padding ->

        // Si está cargando, muestra el iconito de carga
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

        // Muestra la información del evento
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Datos del evento
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
                        // Nombre del evento
                        Text(
                            text = evento?.nombre ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                // Fecha del evento
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Lucide.CalendarDays,
                                        contentDescription = "Fecha",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        " ${evento?.fecha}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                // Lugar del evento
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Lugar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        " ${evento?.lugar}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                // Descripción del evento
                                if (!evento?.descripcion.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        evento?.descripcion ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Código del evento
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "Código",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        evento?.codigoEvento ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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

            // Lista de ponencias
            Text(
                "Ponencias (${listaPonencias.size})",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            // Si no hay ponencias, muestra un mensaje
            if (listaPonencias.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay ponencias todavía",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Pulsa el botón + para añadir una",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                // Si hay ponencias, muestra la lista
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(listaPonencias) { ponencia ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    navController.navigate(
                                        AppScreens.DetallePonenciaOrganizador.createRoute(
                                            ponencia.idPonencia
                                        )
                                    )
                                },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Número de orden
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(end = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${ponencia.orden}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        ponencia.titulo,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        ponencia.ponente,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Lucide.Clock,
                                            contentDescription = "Hora",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            " ${ponencia.horaInicio} - ${ponencia.horaFin}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = {
                                        ponenciaEditando = ponencia
                                        showDialogPonencia = true
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = {
                                        ponenciaEditando = ponencia
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
        }
    }

    // Dialog añadir / editar ponencia
    if (showDialogPonencia) {
        DialogCrearPonencia(
            ponenciaEditando = ponenciaEditando,
            idEvento = idEvento,
            ordenSiguiente = listaPonencias.size + 1,
            onDismiss = {
                showDialogPonencia = false
                ponenciaEditando = null
            },
            onGuardado = { ponenciaNueva ->
                listaPonencias = if (ponenciaEditando == null) {
                    listaPonencias + ponenciaNueva
                } else {
                    listaPonencias.map {
                        if (it.idPonencia == ponenciaNueva.idPonencia) ponenciaNueva else it
                    }
                }
                showDialogPonencia = false
                ponenciaEditando = null
            })
    }

    // Dialog confirmar eliminar
    if (showDialogEliminar && ponenciaEditando != null) {
        AlertDialog(
            onDismissRequest = {
            showDialogEliminar = false
            ponenciaEditando = null
        },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Eliminar ponencia") },
            text = { Text("¿Deseas eliminar \"${ponenciaEditando!!.titulo}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val id = ponenciaEditando!!.idPonencia
                        firestore.collection("ponencias").document(id).delete()
                        ponenciaDao.eliminarPonencia(id)
                        listaPonencias = listaPonencias.filter { it.idPonencia != id }
                        showDialogEliminar = false
                        ponenciaEditando = null
                        Toast.makeText(context, "Ponencia eliminada", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialogEliminar = false
                    ponenciaEditando = null
                }) { Text("Cancelar") }
            })
    }

    if (showQREvento) {
        evento?.let {
            DialogMostrarQR(
                titulo = "QR Check-in — ${it.nombre}",
                contenidoQR = "checkin:${it.idEvento}",
                onDismiss = { showQREvento = false })
        }
    }
}

@Composable
fun DialogCrearPonencia(
    ponenciaEditando: PonenciaData?,
    idEvento: String,
    ordenSiguiente: Int,
    onDismiss: () -> Unit,
    onGuardado: (PonenciaData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val ponenciaDao = db.ponenciaDao()

    var titulo by remember { mutableStateOf(ponenciaEditando?.titulo ?: "") }
    var ponente by remember { mutableStateOf(ponenciaEditando?.ponente ?: "") }
    var descripcion by remember { mutableStateOf(ponenciaEditando?.descripcion ?: "") }
    var horaInicio by remember { mutableStateOf(ponenciaEditando?.horaInicio ?: "") }
    var horaFin by remember { mutableStateOf(ponenciaEditando?.horaFin ?: "") }
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
                    if (ponenciaEditando == null) "Nueva ponencia" else "Editar ponencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ponente,
                    onValueChange = { ponente = it },
                    label = { Text("Ponente") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectorHora(
                        label = "Inicio",
                        hora = horaInicio,
                        onHoraSeleccionada = { horaInicio = it },
                        modifier = Modifier.weight(1f)  // ← ocupa la mitad
                    )
                    SelectorHora(
                        label = "Fin",
                        hora = horaFin,
                        onHoraSeleccionada = { horaFin = it },
                        modifier = Modifier.weight(1f)  // ← ocupa la mitad
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            when {
                                titulo.isBlank() -> Toast.makeText(
                                    context, "Introduce el título", Toast.LENGTH_SHORT
                                ).show()

                                ponente.isBlank() -> Toast.makeText(
                                    context, "Introduce el ponente", Toast.LENGTH_SHORT
                                ).show()

                                horaInicio.isBlank() -> Toast.makeText(
                                    context, "Selecciona la hora de inicio", Toast.LENGTH_SHORT
                                ).show()

                                horaFin.isBlank() -> Toast.makeText(
                                    context, "Selecciona la hora de fin", Toast.LENGTH_SHORT
                                ).show()

                                horaFin <= horaInicio -> Toast.makeText(
                                    context,
                                    "La hora de fin debe ser posterior a la de inicio",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> {
                                    isLoading = true
                                    val idPonencia =
                                        ponenciaEditando?.idPonencia ?: UUID.randomUUID().toString()
                                    val orden = ponenciaEditando?.orden ?: ordenSiguiente
                                    val data = mapOf(
                                        "titulo" to titulo,
                                        "ponente" to ponente,
                                        "descripcion" to descripcion,
                                        "horaInicio" to horaInicio,
                                        "horaFin" to horaFin,
                                        "idEvento" to idEvento,
                                        "orden" to orden,
                                        "qrCode" to (ponenciaEditando?.qrCode ?: "")
                                    )
                                    firestore.collection("ponencias").document(idPonencia).set(data)
                                        .addOnSuccessListener {
                                            val ponenciaNueva = PonenciaData(
                                                idPonencia = idPonencia,
                                                titulo = titulo,
                                                ponente = ponente,
                                                descripcion = descripcion,
                                                horaInicio = horaInicio,
                                                horaFin = horaFin,
                                                qrCode = ponenciaEditando?.qrCode ?: "",
                                                idEvento = idEvento,
                                                orden = orden
                                            )
                                            scope.launch {
                                                ponenciaDao.insertar(ponenciaNueva)
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    if (ponenciaEditando == null) "Ponencia creada" else "Ponencia actualizada",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onGuardado(ponenciaNueva)
                                            }
                                        }.addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(
                                                context, "Error: ${e.message}", Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        }, enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White, modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(if (ponenciaEditando == null) "Crear" else "Guardar")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorHora(
    label: String,
    hora: String,
    onHoraSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier  // ← añadir
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = hora.substringBefore(":").toIntOrNull() ?: 9,
        initialMinute = hora.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )

    OutlinedTextField(
        value = hora,
        onValueChange = {},
        label = { Text(label) },
        placeholder = { Text("00:00") },
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
            }
        },
        readOnly = true,
        singleLine = true,
        modifier = modifier.clickable { showTimePicker = true }  // ← usar modifier
    )

    if (showTimePicker) {
        AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = {
            TextButton(onClick = {
                val hora = "%02d:%02d".format(
                    timePickerState.hour, timePickerState.minute
                )
                onHoraSeleccionada(hora)
                showTimePicker = false
            }) {
                Text("Aceptar")
            }
        }, dismissButton = {
            TextButton(onClick = { showTimePicker = false }) {
                Text("Cancelar")
            }
        }, text = {
            TimePicker(state = timePickerState)
        })
    }
}

@Composable
fun DialogMostrarQR(
    titulo: String, contenidoQR: String, onDismiss: () -> Unit
) {
    val qrBitmap = remember(contenidoQR) {
        generarQRBitmap(contenidoQR)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR",
                    modifier = Modifier.size(250.dp)
                )
                Text(
                    contenidoQR,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}