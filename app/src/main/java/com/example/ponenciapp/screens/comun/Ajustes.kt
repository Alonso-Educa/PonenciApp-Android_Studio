package com.example.ponenciapp.screens.comun

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.composables.icons.lucide.File
import com.composables.icons.lucide.FileSpreadsheet
import com.composables.icons.lucide.Lucide
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.data.bbdd.entities.PonenciaData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.organizador.DialogCrearPonencia
import com.example.ponenciapp.screens.organizador.SelectorHora
import com.example.ponenciapp.screens.participante.formatearFechaHora
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ajustes(navController: NavController) {
    // Para ir a la web
    // Se vinculará la página de ayuda de la parte de flutter cuando se realice
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
    val scope = rememberCoroutineScope()
    val uid = Firebase.auth.currentUser?.uid ?: ""

    // Base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // Dao
    val participanteDao = db.participanteDao()

    var participante by remember { mutableStateOf<ParticipanteData?>(null) }
    var participanteEditando by remember { mutableStateOf<ParticipanteData?>(null) }
    var listaParticipantes by remember { mutableStateOf<List<ParticipanteData>>(emptyList()) }

    var showDialogEditar by remember { mutableStateOf(false) }
    var showDialogCambiarEmail by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        participante = participanteDao.getParticipantePorId(uid)
        isLoading = false
    }

    // Determinar qué BottomBar mostrar según el rol
    val bottomBarAjustes: @Composable () -> Unit = when {
        participante?.rol == "organizador" -> {
            { BottomBarOrganizador(navController, AppScreens.Ajustes.route) }
        }

        participante?.idEvento.isNullOrEmpty() -> {
            { BottomBarUnirseEvento(navController, AppScreens.Ajustes.route) }
        }

        else -> {
            { BottomBarParticipante(navController, AppScreens.Ajustes.route) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Ajustes",
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
        bottomBar = bottomBarAjustes
    ) { padding ->

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Ajustes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()

                    Text(
                        "Datos del usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Puedes editar los datos de tu cuenta aquí",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Tarjeta con los datos del participante
                    participante?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // Cabecera con inicial y botón editar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Círculo con la inicial del participante
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = it.nombre.firstOrNull()?.uppercase() ?: "P",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Nombre y rol
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${it.nombre} ${it.apellidos}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            it.rol.replaceFirstChar { c -> c.uppercase() },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Botón editar
                                    IconButton(onClick = {
                                        participanteEditando = participante
                                        showDialogEditar = true
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar participante",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Datos del participante con iconos
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        it.emailEduca,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${it.centro} — ${it.codigoCentro}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Opciones de cuenta
                                Text(
                                    "Restablecer mis datos privados",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Cambiar email
                                    // Peniente que se sincronice con room bien y no haya conflictos de datos al iniciar sesión TODO()
                                    TextButton(
                                        onClick = { showDialogCambiarEmail = true },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Cambiar email",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Restablecer contraseña
                                    TextButton(
                                        onClick = {
                                            Firebase.auth.sendPasswordResetEmail(
                                                participante?.emailEduca ?: ""
                                            )
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Email de restablecimiento enviado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Restablecer contraseña",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()

                    Text(
                        "Informes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Puedes descargar los informes de tus asistencias aquí",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    exportarAsistenciasExcel(
                                        context, participante?.idEvento ?: ""
                                    )
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Icon(Lucide.FileSpreadsheet, contentDescription = "Excel")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar a Excel")
                        }

//                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    exportarAsistenciasPdf(
                                        context, participante?.idEvento ?: ""
                                    )
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFB71C1C)
                            )
                        ) {
                            Icon(Lucide.File, contentDescription = "PDF")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar a PDF")
                        }
                    }

                    HorizontalDivider()

                    Text(
                        "Otras opciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Si tienes alguna duda, no dudes en ponernos en contacto",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(
                        onClick = {
                            scope.launch {
                                navController.navigate(AppScreens.ChatbotAsistente.route)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chatbot")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuestro chatbot de soporte")
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Abrir la página de ayuda (aún no está hecha)
                    TextButton(
                        onClick = { context.startActivity(intent) }) {
                        Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = "Ayuda")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Página de ayuda")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ArrowOutward, contentDescription = "Salir")
                    }

                    HorizontalDivider()

                    // Cerrar sesión
                    TextButton(
                        onClick = {
                            Firebase.auth.signOut()
                            navController.navigate(AppScreens.Login.route) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        }, colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar sesión")
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Dialog editar participante
        if (showDialogEditar) {
            DialogEditarUsuario(
                participanteEditando = participanteEditando,
                onDismiss = {
                    showDialogEditar = false
                    participanteEditando = null
                },
                // Guardar el participante editado en la base de datos


                onGuardado = { participanteActualizado ->
                    participante = participanteActualizado  // ← actualiza la UI
                    showDialogEditar = false
                    participanteEditando = null

                }
            )
        }

        // Dialog cambiar email
        if (showDialogCambiarEmail) {
            var nuevoEmail by remember { mutableStateOf("") }
            var passwordActual by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

            Dialog(onDismissRequest = { showDialogCambiarEmail = false }) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Cambiar email",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Introduce tu nuevo email. Te enviaremos un enlace de confirmación.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = nuevoEmail,
                            onValueChange = { nuevoEmail = it },
                            label = { Text("Nuevo email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordActual,
                            onValueChange = { passwordActual = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showDialogCambiarEmail = false }) {
                                Text("Cancelar")
                            }
                            Button(onClick = {
                                when {
                                    nuevoEmail.isBlank() -> Toast.makeText(
                                        context,
                                        "Introduce el nuevo email",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    !emailPattern.matches(nuevoEmail) -> Toast.makeText(
                                        context,
                                        "Formato de email inválido",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    nuevoEmail == participante?.emailEduca -> Toast.makeText(
                                        context,
                                        "El email es el mismo que el actual",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    passwordActual.isBlank() -> Toast.makeText(
                                        context,
                                        "Introduce tu contraseña actual",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    else -> {
                                        val user = Firebase.auth.currentUser
                                        val email = participante?.emailEduca ?: ""

                                        // 1. Re-autenticar con las credenciales actuales
                                        val credencial =
                                            EmailAuthProvider.getCredential(email, passwordActual)
                                        user?.reauthenticate(credencial)
                                            ?.addOnSuccessListener {
                                                // 2. Una vez re-autenticado, enviar email de verificación al nuevo correo
                                                user.verifyBeforeUpdateEmail(nuevoEmail)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Email de confirmación enviado a $nuevoEmail",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        showDialogCambiarEmail = false
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(
                                                            context,
                                                            "Error: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            ?.addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Contraseña incorrecta",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }) {
                                Text("Enviar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogEditarUsuario(
    participanteEditando: ParticipanteData?,
    onDismiss: () -> Unit,
    onGuardado: (ParticipanteData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val participanteDao = db.participanteDao()

    var nombre by remember { mutableStateOf(participanteEditando?.nombre ?: "") }
    var apellidos by remember { mutableStateOf(participanteEditando?.apellidos ?: "") }
    var centro by remember { mutableStateOf(participanteEditando?.centro ?: "") }
    var codigoCentro by remember { mutableStateOf(participanteEditando?.codigoCentro ?: "") }
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
                    "Editar datos de participante",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = centro,
                    onValueChange = { centro = it },
                    label = { Text("Centro educativo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = codigoCentro,
                    onValueChange = { codigoCentro = it },
                    label = { Text("Código del centro") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            when {
                                nombre.isBlank() -> Toast.makeText(
                                    context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT
                                ).show()

                                apellidos.isBlank() -> Toast.makeText(
                                    context,
                                    "Los apellidos no pueden estar vacíos",
                                    Toast.LENGTH_SHORT
                                ).show()

                                centro.isBlank() -> Toast.makeText(
                                    context,
                                    "El centro educativo no puede estar vacío",
                                    Toast.LENGTH_SHORT
                                ).show()

                                codigoCentro.isBlank() -> Toast.makeText(
                                    context,
                                    "El código del centro no puede estar vacío",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> {
                                    isLoading = true
                                    val idParticipante =
                                        participanteEditando?.idParticipante ?: ""
                                    val data = mapOf(
                                        "nombre" to nombre,
                                        "apellidos" to apellidos,
                                        "emailEduca" to participanteEditando?.emailEduca,
                                        "centro" to centro,
                                        "codigoCentro" to codigoCentro,
                                        "rol" to (participanteEditando?.rol ?: "participante"),
                                        "fechaRegistro" to participanteEditando?.fechaRegistro,
                                        "idEvento" to participanteEditando?.idEvento
                                    )
                                    firestore.collection("participantes").document(idParticipante)
                                        .set(data)
                                        .addOnSuccessListener {
                                            val participanteNuevo = ParticipanteData(
                                                idParticipante = idParticipante,
                                                nombre = nombre,
                                                apellidos = apellidos,
                                                emailEduca = participanteEditando?.emailEduca ?: "",
                                                centro = centro,
                                                codigoCentro = codigoCentro,
                                                rol = participanteEditando?.rol ?: "participante",
                                                fechaRegistro = participanteEditando?.fechaRegistro
                                                    ?: formatearFechaHora(),
                                                idEvento = participanteEditando?.idEvento ?: ""
                                            )
                                            scope.launch {
                                                participanteDao.insertar(participanteNuevo)
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Participante actualizado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onGuardado(participanteNuevo)
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
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}