package com.example.ponenciapp.screens.comun

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.room.Room
import coil.compose.AsyncImage
import com.composables.icons.lucide.File
import com.composables.icons.lucide.FileSpreadsheet
import com.composables.icons.lucide.Lucide
import com.example.ponenciapp.R
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.UsuarioData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.participante.formatearFechaHora
import com.example.ponenciapp.screens.utilidad.IconoUsuario
import com.example.ponenciapp.screens.utilidad.ThemeViewModel
import com.example.ponenciapp.screens.utilidad.exportarAsistenciasExcel
import com.example.ponenciapp.screens.utilidad.exportarAsistenciasPdf
import com.example.ponenciapp.screens.utilidad.subirImagenCloudinary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ajustes(navController: NavController, themeViewModel: ThemeViewModel) {
    // Para ir a la web
    // Se vinculará la página de ayuda de la parte de flutter cuando se realice
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ponenciapp.web.app/ayuda.html"))
    val scope = rememberCoroutineScope()
    val uid = Firebase.auth.currentUser?.uid ?: ""

    val activity = context as Activity
    val webClientId = stringResource(R.string.default_web_client_id)

    @Suppress("NewApi")
    val credentialManager = remember { CredentialManager.create(context) }

    // Base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // Dao
    val usuarioDao = db.usuarioDao()

    var participante by remember { mutableStateOf<UsuarioData?>(null) }
    var participanteEditando by remember { mutableStateOf<UsuarioData?>(null) }

    var showDialogEditar by remember { mutableStateOf(false) }
    var showDialogCambiarEmail by remember { mutableStateOf(false) }
    var showDialogBorrarCuenta by remember { mutableStateOf(false) }
    var showDialogCerrarSesion by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        participante = usuarioDao.getParticipantePorId(uid)
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

    // Para subir imágenes desde la galería
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->

            // Subimos la imagen a Cloudinary
            subirImagenCloudinary(context, selectedUri, uid) { imageUrl ->

                // Actualización de Firestore primero
                Firebase.firestore.collection("usuarios")
                    .document(uid)
                    .update("fotoPerfilUrl", imageUrl)
                    .addOnSuccessListener {
                        // Actualización de Room dentro de coroutine
                        scope.launch {
                            participante?.let { p ->
                                val actualizado = p.copy(fotoPerfilUrl = imageUrl)
                                usuarioDao.actualizar(actualizado) // suspend, ok dentro de coroutine
                                participante = actualizado
                            }
                        }
                    }
            }
            showDialogEditar = false
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
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    participante?.let {
                        IconoUsuario(
                            usuario = it
                        )
                    }
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // Cabecera con inicial y botón editar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Imagen de perfil si la hay
                                    if (!it.fotoPerfilUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = it.fotoPerfilUrl,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Inicial si no hay foto
                                        val inicial = it.nombre.firstOrNull()?.uppercase() ?: "U"
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Valida que el usuario tenga login con email y contraseña
                                val puedeGestionarCredenciales = Firebase.auth.currentUser
                                    ?.providerData
                                    ?.any { it.providerId == "password" } == true

                                // Si lo tiene, muestra el menú de gestión de credenciales
                                if (puedeGestionarCredenciales) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    // Opciones de cuenta
                                    Text(
                                        "Restablecer mis datos privados",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Cambiar email
                                        // Pendiente que se sincronice con room bien y no haya conflictos de datos al iniciar sesión TODO()
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
                                                "Cambiar correo",
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
            }

            item {
                // Estados
                val currentUser = Firebase.auth.currentUser
                val proveedoresVinculados =
                    currentUser?.providerData?.map { it.providerId } ?: emptyList()
                val tieneEmailPassword = "password" in proveedoresVinculados
                val tieneGoogle = "google.com" in proveedoresVinculados
                val tieneMicrosoft = "microsoft.com" in proveedoresVinculados

                // Solo mostramos esta sección si tiene email/password (puede vincular OAuth)
                // o si tiene OAuth y puede vincular más
                val hayAlgoQueVincular = !tieneGoogle || !tieneMicrosoft

                if (tieneEmailPassword && hayAlgoQueVincular) {

                    var mostrarDialogo by remember { mutableStateOf(false) }
                    var proveedorSeleccionado by remember { mutableStateOf("") }
                    var estaCargando by remember { mutableStateOf(false) }

                    if (mostrarDialogo) {
                        DialogoVincularProveedor(
                            proveedorNombre = proveedorSeleccionado,
                            onConfirmar = { password ->
                                mostrarDialogo = false
                                estaCargando = true
                                val emailActual =
                                    currentUser?.email ?: return@DialogoVincularProveedor

                                when (proveedorSeleccionado) {
                                    "Google" -> {
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setServerClientId(webClientId)
                                            .setFilterByAuthorizedAccounts(false)
                                            .build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        scope.launch {
                                            try {
                                                val result = credentialManager.getCredential(
                                                    context,
                                                    request
                                                )
                                                val credential = result.credential
                                                if (credential is CustomCredential &&
                                                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                                ) {
                                                    val idToken =
                                                        GoogleIdTokenCredential.createFrom(
                                                            credential.data
                                                        ).idToken
                                                    val googleCredential =
                                                        GoogleAuthProvider.getCredential(
                                                            idToken,
                                                            null
                                                        )
                                                    reautenticarYVincular(
                                                        auth = Firebase.auth,
                                                        email = emailActual,
                                                        password = password,
                                                        credentialToLink = googleCredential,
                                                        context = context,
                                                        onExito = { estaCargando = false },
                                                        onError = { msg ->
                                                            estaCargando = false
                                                            Toast.makeText(
                                                                context,
                                                                msg,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    )
                                                }
                                            } catch (e: GetCredentialException) {
                                                estaCargando = false
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${e.localizedMessage}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }

                                    "Microsoft" -> {
                                        val provider = OAuthProvider.newBuilder("microsoft.com")
                                            .addCustomParameter("tenant", "common")
                                            .setScopes(
                                                listOf(
                                                    "email",
                                                    "profile",
                                                    "openid",
                                                    "User.Read"
                                                )
                                            )
                                            .build()

                                        Firebase.auth.currentUser
                                            ?.reauthenticate(
                                                EmailAuthProvider.getCredential(
                                                    emailActual,
                                                    password
                                                )
                                            )
                                            ?.addOnSuccessListener {
                                                Firebase.auth.currentUser
                                                    ?.startActivityForLinkWithProvider(
                                                        activity,
                                                        provider
                                                    )
                                                    ?.addOnSuccessListener {
                                                        estaCargando = false
                                                        Toast.makeText(
                                                            context,
                                                            "Cuenta de Microsoft vinculada",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    ?.addOnFailureListener { e ->
                                                        estaCargando = false
                                                        Toast.makeText(
                                                            context,
                                                            "Error: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            ?.addOnFailureListener {
                                                estaCargando = false
                                                Toast.makeText(
                                                    context,
                                                    "Contraseña incorrecta",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            },
                            onDismiss = { mostrarDialogo = false }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider()

                        Text(
                            "Métodos de acceso vinculados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Vincula proveedores adicionales para acceder con ellos también",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!tieneGoogle) {
                                OutlinedButton(
                                    onClick = {
                                        proveedorSeleccionado = "Google"
                                        mostrarDialogo = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !estaCargando
                                ) {
                                    if (estaCargando && proveedorSeleccionado == "Google") {
                                        CircularProgressIndicator(Modifier.size(16.dp))
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.google_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Unspecified
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Vincular Google",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            if (!tieneMicrosoft) {
                                OutlinedButton(
                                    onClick = {
                                        proveedorSeleccionado = "Microsoft"
                                        mostrarDialogo = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !estaCargando
                                ) {
                                    if (estaCargando && proveedorSeleccionado == "Microsoft") {
                                        CircularProgressIndicator(Modifier.size(16.dp))
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.microsoft_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Unspecified
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Vincular Microsoft",
                                            style = MaterialTheme.typography.bodySmall
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
                    // Hacer que el organizador pueda descargar informes de sus eventos
                    // o de asistencias de participantes a estos TODO()
                    if (participante?.rol == "participante") {
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
                            // Botón de exportar a excel
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

                            // Botón de exportar a pdf
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
                    }

                    HorizontalDivider()

                    Text(
                        "Personalización",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Cambia el aspecto de la aplicación",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Modo oscuro",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Switch(
                            checked = themeViewModel.isDarkTheme,
                            onCheckedChange = {
                                themeViewModel.updateDarkTheme(it)
                            }
                        )
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

                    Text(
                        "Gestión de cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Puedes eliminar permanentemente tu cuenta y todos tus datos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Borrar cuenta
                    OutlinedButton(
                        onClick = { showDialogBorrarCuenta = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar cuenta")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Borrar cuenta")
                    }

                    HorizontalDivider()

                    // Cerrar sesión
                    TextButton(
                        onClick = {
                            showDialogCerrarSesion = true
                        },
                        colors = ButtonDefaults.textButtonColors(
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
                },
                launcher = launcher
            )
        }

        // Dialog cambiar email
        if (showDialogCambiarEmail) {
            var nuevoEmail by remember { mutableStateOf("") }
            var passwordActual by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            val emailPattern = Regex(
                "^[\\p{L}\\p{N}._%+\\-]+@[\\p{L}\\p{N}_\\-]+(\\.[\\p{L}\\p{N}_\\-]+)*\\.\\p{L}{2,}$"
            )
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
                            "Cambiar correo electrónico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Introduce tu nuevo correo. Te enviaremos un enlace de confirmación. Por favor inicie sesión de nuevo tras hacer el cambio con éxito.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = nuevoEmail,
                            onValueChange = { nuevoEmail = it.filterNot { char -> char.isWhitespace() } },
                            label = { Text("Nuevo correo") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordActual,
                            onValueChange = { passwordActual = it.filterNot { char -> char.isWhitespace() } },
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

        if (showDialogBorrarCuenta) {

            var password by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            val user = Firebase.auth.currentUser
            val proveedores = user?.providerData?.map { it.providerId } ?: emptyList()
            val tienePassword = "password" in proveedores
            val tieneGoogle = "google.com" in proveedores
            val tieneMicrosoft = "microsoft.com" in proveedores

            fun borrarCuentaTrasReautenticacion() {
                val uid = user?.uid ?: return
                isLoading = true

                Firebase.firestore.collection("usuarios")
                    .document(uid)
                    .delete()
                    .addOnSuccessListener {
                        user.delete()
                            .addOnSuccessListener {
                                scope.launch { usuarioDao.eliminar(uid) }
                                isLoading = false
                                showDialogBorrarCuenta = false
                                Toast.makeText(
                                    context,
                                    "Cuenta borrada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(AppScreens.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error borrando auth: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Toast.makeText(
                            context,
                            "Error borrando datos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            fun reautenticarYBorrar() {
                val uid = user?.uid ?: return

                when {
                    tienePassword -> {
                        val email = user.email ?: return
                        val credential = EmailAuthProvider.getCredential(email, password)
                        user.reauthenticate(credential)
                            .addOnSuccessListener { borrarCuentaTrasReautenticacion() }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }

                    tieneGoogle -> {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(webClientId)
                            .setFilterByAuthorizedAccounts(true)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        scope.launch {
                            try {
                                val result = credentialManager.getCredential(context, request)
                                val credential = result.credential
                                if (credential is CustomCredential &&
                                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {
                                    val idToken =
                                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    val googleCredential =
                                        GoogleAuthProvider.getCredential(idToken, null)
                                    user.reauthenticate(googleCredential)
                                        .addOnSuccessListener { borrarCuentaTrasReautenticacion() }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Error al verificar Google: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } catch (e: GetCredentialException) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    tieneMicrosoft -> {
                        val provider = OAuthProvider.newBuilder("microsoft.com")
                            .addCustomParameter("tenant", "common")
                            .setScopes(listOf("email", "profile", "openid", "User.Read"))
                            .build()

                        user.startActivityForReauthenticateWithProvider(activity, provider)
                            .addOnSuccessListener { borrarCuentaTrasReautenticacion() }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error al verificar Microsoft: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }

            Dialog(onDismissRequest = { if (!isLoading) showDialogBorrarCuenta = false }) {
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
                            "Borrar mi cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            when {
                                tienePassword -> "Para continuar, confirma tu contraseña. Esta acción es irreversible."
                                tieneGoogle -> "Se te pedirá que confirmes tu cuenta de Google. Esta acción es irreversible."
                                tieneMicrosoft -> "Se te pedirá que confirmes tu cuenta de Microsoft. Esta acción es irreversible."
                                else -> "Esta acción es irreversible."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Campo de contraseña solo si tiene email/password
                        if (tienePassword) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it.filterNot { char -> char.isWhitespace() } },
                                label = { Text("Contraseña") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { showDialogBorrarCuenta = false },
                                enabled = !isLoading
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                enabled = (!tienePassword || password.isNotBlank()) && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                onClick = { reautenticarYBorrar() }
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                } else {
                                    Text("Aceptar")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialogCerrarSesion) {

            AlertDialog(
                onDismissRequest = { showDialogCerrarSesion = false },
                title = { Text("Cerrar sesión") },
                text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                confirmButton = {
                    Button(
                        onClick = {
                            Firebase.auth.signOut()

                            navController.navigate(AppScreens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }

                            showDialogCerrarSesion = false

                            Toast.makeText(
                                context,
                                "Sesión cerrada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cerrar sesión")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialogCerrarSesion = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun DialogEditarUsuario(
    participanteEditando: UsuarioData?,
    onDismiss: () -> Unit,
    onGuardado: (UsuarioData) -> Unit,
    launcher: ManagedActivityResultLauncher<String, Uri?>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val usuarioDao = db.usuarioDao()

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
                TextButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Cambiar foto de usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                                        participanteEditando?.idUsuario ?: ""
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
                                    firestore.collection("usuarios").document(idParticipante)
                                        .set(data)
                                        .addOnSuccessListener {
                                            val participanteNuevo = UsuarioData(
                                                idUsuario = idParticipante,
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
                                                usuarioDao.insertar(participanteNuevo)
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

// Función para vincular un proveedor a la cuenta existente
@Composable
fun DialogoVincularProveedor(
    proveedorNombre: String,
    onConfirmar: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vincular con $proveedorNombre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Por seguridad, confirma tu contraseña actual antes de vincular una nueva cuenta.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filterNot { char -> char.isWhitespace() } },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(password) },
                enabled = password.isNotBlank()
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

fun reautenticarYVincular(
    auth: FirebaseAuth,
    email: String,
    password: String,
    credentialToLink: AuthCredential,
    context: Context,
    onExito: () -> Unit,
    onError: (String) -> Unit
) {
    val emailCredential = EmailAuthProvider.getCredential(email, password)
    auth.currentUser?.reauthenticate(emailCredential)
        ?.addOnSuccessListener {
            auth.currentUser?.linkWithCredential(credentialToLink)
                ?.addOnSuccessListener {
                    Toast.makeText(context, "Cuenta vinculada correctamente", Toast.LENGTH_SHORT)
                        .show()
                    onExito()
                }
                ?.addOnFailureListener { e ->
                    onError(e.message ?: "Error al vincular")
                }
        }
        ?.addOnFailureListener {
            onError("Contraseña incorrecta")
        }
}