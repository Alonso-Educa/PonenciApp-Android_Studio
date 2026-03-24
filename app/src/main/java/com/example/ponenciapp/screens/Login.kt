package com.example.ponenciapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.notification.NotificationHandler
import com.example.ponenciapp.screens.participante.formatearFechaHora
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun Login(navController: NavController) {

    // Variables usadas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val firestore = FirebaseFirestore.getInstance()

    // Base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val participanteDao = db.participanteDao()

    // Campos de texto
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var estaCargando by remember { mutableStateOf(false) }
    var showDialogRecuperar by remember { mutableStateOf(false) }

    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    var showDialogRegistro by remember { mutableStateOf(false) }

    // Contenido del login
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo y título
        Text(
            text = "PonenciApp",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Accede con tu email y contraseña del evento",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email educativo") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
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

        Spacer(modifier = Modifier.height(8.dp))

        // Recuperar contraseña
        TextButton(
            onClick = { showDialogRecuperar = true }, modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Has olvidado tu contraseña?")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de iniciar sesión
        Button(
            onClick = {
                when {
                    email.isBlank() -> Toast.makeText(
                        context, "Introduce tu email", Toast.LENGTH_SHORT
                    ).show()

                    !emailPattern.matches(email) -> Toast.makeText(
                        context, "Formato de email inválido", Toast.LENGTH_SHORT
                    ).show()

                    contrasena.isBlank() -> Toast.makeText(
                        context, "Introduce tu contraseña", Toast.LENGTH_SHORT
                    ).show()

                    contrasena.length < 6 -> Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        estaCargando = true
                        auth.signInWithEmailAndPassword(email, contrasena)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: ""
                                // Email actual en Firebase Auth
                                val emailActualAuth = result.user?.email ?: ""
                                firestore.collection("participantes").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        if (!doc.exists()) {
                                            estaCargando = false
                                            Toast.makeText(
                                                context,
                                                "No se encontraron datos del usuario",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@addOnSuccessListener
                                        }
                                        val emailEnFirestore = doc.getString("emailEduca") ?: ""

                                        // Si el email de Auth es diferente al de Firestore, actualizar Firestore
                                        if (emailActualAuth != emailEnFirestore) {
                                            firestore.collection("participantes").document(uid)
                                                .update("emailEduca", emailActualAuth)
                                        }
                                        scope.launch {
                                            participanteDao.insertar(
                                                ParticipanteData(
                                                    idParticipante = uid,
                                                    nombre = doc.getString("nombre") ?: "",
                                                    apellidos = doc.getString("apellidos") ?: "",
                                                    emailEduca = doc.getString("emailEduca") ?: "", // ← se actualiza al hacer login
                                                    centro = doc.getString("centro") ?: "",
                                                    codigoCentro = doc.getString("codigoCentro") ?: "",
                                                    rol = doc.getString("rol") ?: "participante",
                                                    fechaRegistro = doc.getString("fechaRegistro") ?: "",
                                                    idEvento = doc.getString("idEvento") ?: ""
                                                )
                                            )
                                            estaCargando = false
                                            navController.navigate(AppScreens.PantallaPrincipal.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }.addOnFailureListener { e ->
                                        estaCargando = false
                                        Toast.makeText(
                                            context,
                                            "Error obteniendo datos del usuario: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }.addOnFailureListener {
                                estaCargando = false
                                Toast.makeText(
                                    context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), enabled = !estaCargando
        ) {
            // Si está cargando muestra un iconito de carga
            if (estaCargando) {
                CircularProgressIndicator(
                    color = Color.White, modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Iniciar sesión", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de registrarse. quizá se quite más adelante
        HorizontalDivider()
        Text(
            "Si no tienes una cuenta, regístrate aquí",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        OutlinedButton(
            onClick = { showDialogRegistro = true }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }

    // Dialog de recuperar contraseña
    if (showDialogRecuperar) {
        DialogRecuperarContrasena(
            onDismiss = { showDialogRecuperar = false })
    }


    // Muestra el dialog de registro
    if (showDialogRegistro) {
        DialogRegistro(onDismiss = { showDialogRegistro = false })
    }
}

// Función para mostrar el dialog de recuperar contraseña
@Composable
fun DialogRecuperarContrasena(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val auth = Firebase.auth
    var emailRecuperar by remember { mutableStateOf("") }
    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    Dialog(onDismissRequest = onDismiss) {
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
                    "Recuperar contraseña",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Introduce tu email y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = emailRecuperar,
                    onValueChange = { emailRecuperar = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        when {
                            emailRecuperar.isBlank() -> Toast.makeText(
                                context, "Introduce tu email", Toast.LENGTH_SHORT
                            ).show()

                            !emailPattern.matches(emailRecuperar) -> Toast.makeText(
                                context, "Formato de email inválido", Toast.LENGTH_SHORT
                            ).show()

                            else -> {
                                auth.sendPasswordResetEmail(emailRecuperar).addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Email de recuperación enviado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            context, "Error enviando el email", Toast.LENGTH_SHORT
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

// Función para mostrar el dialog de registro
@Composable
fun DialogRegistro(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val participanteDao = db.participanteDao()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var emailEduca by remember { mutableStateOf("") }
    var centro by remember { mutableStateOf("") }
    var codigoCentro by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    var isLoading by remember { mutableStateOf(false) }

    val notificationHandler = NotificationHandler(context)

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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Registra tu cuenta",
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emailEduca,
                    onValueChange = { emailEduca = it },
                    label = { Text("Email educativo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = centro,
                    onValueChange = { centro = it },
                    label = { Text("Centro educativo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = codigoCentro,
                    onValueChange = { codigoCentro = it },
                    label = { Text("Código de centro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility, contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            when {
                                nombre.isBlank() -> Toast.makeText(
                                    context, "Introduce el nombre", Toast.LENGTH_SHORT
                                ).show()

                                apellidos.isBlank() -> Toast.makeText(
                                    context, "Introduce los apellidos", Toast.LENGTH_SHORT
                                ).show()

                                emailEduca.isBlank() -> Toast.makeText(
                                    context, "Introduce el email", Toast.LENGTH_SHORT
                                ).show()

                                !emailPattern.matches(emailEduca) -> Toast.makeText(
                                    context, "Formato de email inválido", Toast.LENGTH_SHORT
                                ).show()

                                centro.isBlank() -> Toast.makeText(
                                    context, "Introduce el centro", Toast.LENGTH_SHORT
                                ).show()

                                codigoCentro.isBlank() -> Toast.makeText(
                                    context, "Introduce el código de centro", Toast.LENGTH_SHORT
                                ).show()

                                // Cambiar minimo de caracteres y pedir caracter especial TODO
                                password.length < 6 -> Toast.makeText(
                                    context,
                                    "La contraseña debe tener al menos 6 caracteres",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(emailEduca, password)
                                        .addOnSuccessListener { result ->
                                            val uid = result.user?.uid ?: ""
                                            val data = mapOf(
                                                "nombre" to nombre,
                                                "apellidos" to apellidos,
                                                "emailEduca" to emailEduca,
                                                "centro" to centro,
                                                "codigoCentro" to codigoCentro,
                                                "rol" to "participante",
                                                "fechaRegistro" to formatearFechaHora()
                                            )
                                            firestore.collection("participantes").document(uid)
                                                .set(data).addOnSuccessListener {
                                                    scope.launch {
                                                        participanteDao.insertar(
                                                            ParticipanteData(
                                                                idParticipante = uid,
                                                                nombre = nombre,
                                                                apellidos = apellidos,
                                                                emailEduca = emailEduca,
                                                                centro = centro,
                                                                codigoCentro = codigoCentro,
                                                                rol = "participante",
                                                                fechaRegistro = formatearFechaHora()
                                                            )
                                                        )
                                                        isLoading = false
                                                        Toast.makeText(
                                                            context,
                                                            "Cuenta creada correctamente",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        onDismiss()
                                                        // Cerrar sesión tras registrar para que el usuario haga login manualmente
                                                        auth.signOut()
                                                        notificationHandler.enviarNotificacionSimple(
                                                            "Registro exitoso", "¡Bienvenido a PonenciApp, $nombre!"
                                                        )
                                                    }
                                                }.addOnFailureListener { e ->
                                                    isLoading = false
                                                    // Si falla Firestore, elimina la cuenta de Auth para no dejar datos sin registrar
                                                    auth.currentUser?.delete()
                                                    Toast.makeText(
                                                        context,
                                                        "Error guardando datos: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }.addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Error creando cuenta: ${e.message}",
                                                Toast.LENGTH_SHORT
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
                            Text("Registrar")
                        }
                    }
                }
            }
        }
    }
}