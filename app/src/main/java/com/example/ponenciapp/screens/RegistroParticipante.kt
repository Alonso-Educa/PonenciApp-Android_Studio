package com.example.ponenciapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.dao.UsuarioDao
import com.example.ponenciapp.data.bbdd.entities.UsuarioData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.notification.NotificationHandler
import com.example.ponenciapp.screens.participante.formatearFechaHora
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroParticipante(
    navController: NavController,
    proveedor: String,       // "email", "google" o "microsoft"
    uid: String,            // UID ya creado en Firebase Auth (vacío si es email)
    emailExterno: String,   // Email del proveedor externo (vacío si es email)
    displayName: String,     // DisplayName del proveedor externo (vacío si es email)
    fotoUrlExterno: String // URL de la foto del proveedor externo (vacío si es email)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val firestore = FirebaseFirestore.getInstance()
    val notificationHandler = NotificationHandler(context)

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val usuarioDao = db.usuarioDao()

    val esProveedorExterno = proveedor == "google" || proveedor == "microsoft"

    // Campos: si viene de proveedor externo, email y nombre vienen pre-rellenados
    var nombre by remember { mutableStateOf(if (esProveedorExterno) displayName else "") }
    var apellidos by remember { mutableStateOf("") }
    var emailEduca by remember { mutableStateOf(if (esProveedorExterno) emailExterno else "") }
    var centro by remember { mutableStateOf("") }
    var codigoCentro by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf(if (esProveedorExterno) fotoUrlExterno else "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val emailPattern = Regex(
        "^[\\p{L}\\p{N}._%+\\-]+@[\\p{L}\\p{N}_\\-]+(\\.[\\p{L}\\p{N}_\\-]+)*\\.[\\p{L}]{2,}$"
    )

    // Etiqueta del proveedor para mostrar en UI
    val proveedorEtiqueta = when (proveedor) {
        "google" -> "Google"
        "microsoft" -> "Microsoft (Educacyl)"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de usuario") },
                navigationIcon = {
                    // Solo permite volver si es registro de email, no de OAuth
                    // Si viene de OAuth ya tiene cuenta creada en Auth y necesita completarla
                    if (!esProveedorExterno) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera informativa
            if (proveedorEtiqueta != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Cuenta $proveedorEtiqueta verificada. Completa los datos para continuar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    "Crea tu cuenta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Email: no editable si viene de proveedor externo
            OutlinedTextField(
                value = emailEduca,
                onValueChange = {
                    if (!esProveedorExterno) emailEduca =
                        it.filterNot { char -> char.isWhitespace() }
                },
                label = { Text("Email educativo") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                trailingIcon = {
                    if (esProveedorExterno) {
                        Icon(
                            Icons.Default.Lock, contentDescription = "No editable",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                readOnly = esProveedorExterno,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = if (esProveedorExterno) OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) else OutlinedTextFieldDefaults.colors()
            )

            // Nombre: pre-relleno si viene de OAuth pero editable
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = {
                    Text(if (esProveedorExterno) "Nombre (edita si es necesario)" else "Nombre")
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = centro,
                onValueChange = { centro = it },
                label = { Text("Centro educativo") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = codigoCentro,
                onValueChange = { codigoCentro = it },
                label = { Text("Código de centro") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Contraseña: solo para registro con email
            if (!esProveedorExterno) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filterNot { char -> char.isWhitespace() } },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        // Validaciones comunes
                        nombre.isBlank() -> Toast.makeText(
                            context,
                            "Introduce el nombre",
                            Toast.LENGTH_SHORT
                        ).show()

                        apellidos.isBlank() -> Toast.makeText(
                            context,
                            "Introduce los apellidos",
                            Toast.LENGTH_SHORT
                        ).show()

                        emailEduca.isBlank() -> Toast.makeText(
                            context,
                            "Introduce el email",
                            Toast.LENGTH_SHORT
                        ).show()

                        !emailPattern.matches(emailEduca) -> Toast.makeText(
                            context,
                            "Formato de email inválido",
                            Toast.LENGTH_SHORT
                        ).show()

                        centro.isBlank() -> Toast.makeText(
                            context,
                            "Introduce el centro",
                            Toast.LENGTH_SHORT
                        ).show()

                        codigoCentro.isBlank() -> Toast.makeText(
                            context,
                            "Introduce el código de centro",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Estas dos validaciones solo se aplican a usuarios registrados en la app
                        !esProveedorExterno && password.length < 10 -> Toast.makeText(
                            context,
                            "La contraseña debe tener al menos 10 caracteres",
                            Toast.LENGTH_SHORT
                        ).show()

                        !esProveedorExterno && !password.any { !it.isLetterOrDigit() } -> Toast.makeText(
                            context,
                            "La contraseña debe contener al menos un carácter especial",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> {
                            isLoading = true
                            if (esProveedorExterno) {
                                // El usuario ya existe en Firebase Auth, solo guardar en Firestore y Room
                                guardarDatosNuevoUsuario(
                                    uid = uid,
                                    nombre = nombre,
                                    apellidos = apellidos,
                                    emailEduca = emailEduca,
                                    centro = centro,
                                    codigoCentro = codigoCentro,
                                    firestore = firestore,
                                    usuarioDao = usuarioDao,
                                    scope = scope,
                                    onSuccess = {
                                        isLoading = false
                                        notificationHandler.enviarNotificacionSimple(
                                            "Registro exitoso", "¡Bienvenido a PonenciApp, $nombre!"
                                        )
                                        navController.navigate(AppScreens.PantallaPrincipal.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onError = { msg ->
                                        isLoading = false
                                        // Si falla, eliminar la cuenta de Auth para no dejar estado inconsistente
                                        auth.currentUser?.delete()
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    fotoUrl = fotoUrl
                                )
                            } else {
                                // Registro con email: crear cuenta en Firebase Auth primero
                                auth.createUserWithEmailAndPassword(emailEduca, password)
                                    .addOnSuccessListener { result ->
                                        val newUid = result.user?.uid ?: ""
                                        guardarDatosNuevoUsuario(
                                            uid = newUid,
                                            nombre = nombre,
                                            apellidos = apellidos,
                                            emailEduca = emailEduca,
                                            centro = centro,
                                            codigoCentro = codigoCentro,
                                            firestore = firestore,
                                            usuarioDao = usuarioDao,
                                            scope = scope,
                                            onSuccess = {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Cuenta creada correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                auth.signOut()
                                                notificationHandler.enviarNotificacionSimple(
                                                    "Registro exitoso",
                                                    "¡Bienvenido a PonenciApp, $nombre!"
                                                )
                                                navController.popBackStack()
                                            },
                                            onError = { msg ->
                                                isLoading = false
                                                auth.currentUser?.delete()
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                                    .show()
                                            },
                                            fotoUrl = fotoUrl
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Error creando cuenta: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (esProveedorExterno) "Completar registro" else "Registrarse",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Función reutilizable para guardar datos en Firestore + Room
private fun guardarDatosNuevoUsuario(
    uid: String,
    nombre: String,
    apellidos: String,
    emailEduca: String,
    centro: String,
    codigoCentro: String,
    firestore: FirebaseFirestore,
    usuarioDao: UsuarioDao,
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    fotoUrl: String
) {
    val data = mapOf(
        "nombre" to nombre,
        "apellidos" to apellidos,
        "emailEduca" to emailEduca,
        "centro" to centro,
        "codigoCentro" to codigoCentro,
        "rol" to "participante",
        "fechaRegistro" to formatearFechaHora(),
        "idEvento" to "",
        "fotoPerfilUrl" to fotoUrl
    )
    firestore.collection("usuarios").document(uid)
        .set(data)
        .addOnSuccessListener {
            scope.launch {
                usuarioDao.insertar(
                    UsuarioData(
                        idUsuario = uid,
                        nombre = nombre,
                        apellidos = apellidos,
                        emailEduca = emailEduca,
                        centro = centro,
                        codigoCentro = codigoCentro,
                        rol = "participante",
                        fechaRegistro = formatearFechaHora(),
                        idEvento = "",
                        fotoPerfilUrl = fotoUrl
                    )
                )
                onSuccess()
            }
        }
        .addOnFailureListener { e ->
            onError("Error guardando datos: ${e.message}")
        }
}