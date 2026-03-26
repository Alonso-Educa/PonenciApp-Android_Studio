package com.example.ponenciapp.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.R
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.utilidad.ThemeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.ponenciapp.data.bbdd.dao.ParticipanteDao
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.CoroutineScope
import android.content.Context

@Composable
fun Login(navController: NavController, themeViewModel: ThemeViewModel) {

    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val participanteDao = db.participanteDao()

    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var estaCargandoEmail by remember { mutableStateOf(false) }
    var estaCargandoGoogle by remember { mutableStateOf(false) }
    var estaCargandoMicrosoft by remember { mutableStateOf(false) }
    var showDialogRecuperar by remember { mutableStateOf(false) }

    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Google Sign-In
    val credentialManager = remember { CredentialManager.create(context) }

    @Suppress("LocalContextGetResourceValueCall")
    val webClientId = remember {
        context.getString(R.string.default_web_client_id)
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo y título
        Image(
            painter = if (themeViewModel.isDarkTheme) painterResource(R.drawable.logotemaoscuro)
            else painterResource(R.drawable.logotemaclaro),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
        )
        Text(
            text = "PonenciApp",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Accede con tu email y contraseña al evento",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

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

        TextButton(
            onClick = { showDialogRecuperar = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Has olvidado tu contraseña?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de iniciar sesión con email/contraseña
        Button(
            onClick = {
                when {
                    email.isBlank() -> Toast.makeText(
                        context,
                        "Introduce tu email",
                        Toast.LENGTH_SHORT
                    ).show()

                    !emailPattern.matches(email) -> Toast.makeText(
                        context,
                        "Formato de email inválido",
                        Toast.LENGTH_SHORT
                    ).show()

                    contrasena.isBlank() -> Toast.makeText(
                        context,
                        "Introduce tu contraseña",
                        Toast.LENGTH_SHORT
                    ).show()

                    contrasena.length < 6 -> Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()

                    // descomentar esto para validar contraseña correctamente TODO()
//                    contrasena.length < 10 -> Toast.makeText(
//                        context,
//                        "La contraseña debe tener al menos 10 caracteres",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    !contrasena.any { !it.isLetterOrDigit() } -> Toast.makeText(
//                        context,
//                        "La contraseña debe contener al menos un carácter especial",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    else -> {
                        estaCargandoEmail = true
                        auth.signInWithEmailAndPassword(email, contrasena)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: ""
                                val emailActualAuth = result.user?.email ?: ""
                                firestore.collection("participantes").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        if (!doc.exists()) {
                                            estaCargandoEmail = false
                                            Toast.makeText(
                                                context,
                                                "No se encontraron datos del usuario",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@addOnSuccessListener
                                        }
                                        val emailEnFirestore = doc.getString("emailEduca") ?: ""
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
                                                    emailEduca = doc.getString("emailEduca") ?: "",
                                                    centro = doc.getString("centro") ?: "",
                                                    codigoCentro = doc.getString("codigoCentro")
                                                        ?: "",
                                                    rol = doc.getString("rol") ?: "participante",
                                                    fechaRegistro = doc.getString("fechaRegistro")
                                                        ?: "",
                                                    idEvento = doc.getString("idEvento") ?: ""
                                                )
                                            )
                                            estaCargandoEmail = false
                                            navController.navigate(AppScreens.PantallaPrincipal.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }.addOnFailureListener { e ->
                                        estaCargandoEmail = false
                                        Toast.makeText(
                                            context,
                                            "Error obteniendo datos: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }.addOnFailureListener {
                                estaCargandoEmail = false
                                Toast.makeText(
                                    context,
                                    "Email o contraseña incorrectos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !estaCargandoEmail && !estaCargandoGoogle && !estaCargandoMicrosoft
        ) {
            if (estaCargandoEmail) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Iniciar sesión", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "O inicia sesión con",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Fila de botones para login externo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón de login con Google
            OutlinedButton(
                onClick = {
                    estaCargandoGoogle = true
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
                                context = context,
                                request = request
                            )
                            val credential = result.credential
                            if (credential is CustomCredential &&
                                credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                val firebaseCredential = GoogleAuthProvider.getCredential(
                                    googleIdTokenCredential.idToken, null
                                )
                                auth.signInWithCredential(firebaseCredential)
                                    .addOnSuccessListener { authResult ->
                                        val uid = authResult.user?.uid ?: ""
                                        val emailUser = authResult.user?.email ?: ""
                                        val displayName = authResult.user?.displayName ?: ""
                                        handleOAuthLoginSuccess(
                                            uid = uid,
                                            emailUser = emailUser,
                                            displayName = displayName,
                                            provider = "google",
                                            firestore = firestore,
                                            participanteDao = participanteDao,
                                            scope = scope,
                                            navController = navController,
                                            context = context,
                                            onCargando = {
                                                estaCargandoGoogle = false
                                                estaCargandoMicrosoft = false
                                            }
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        estaCargandoGoogle = false
                                        Toast.makeText(
                                            context,
                                            "Error con Google: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                estaCargandoGoogle = false
                                Toast.makeText(
                                    context,
                                    "Credencial no reconocida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: GetCredentialException) {
                            estaCargandoGoogle = false
                            Toast.makeText(
                                context,
                                "Error con Google: ${e.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                enabled = !estaCargandoEmail && !estaCargandoGoogle && !estaCargandoMicrosoft,
                contentPadding = PaddingValues(8.dp)
            ) {
                if (estaCargandoGoogle) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Google",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Botón de login con microsoft
            OutlinedButton(
                onClick = {
                    estaCargandoMicrosoft = true
                    val provider = OAuthProvider.newBuilder("microsoft.com")
                        .addCustomParameter("tenant", "common")
                        .setScopes(listOf("email", "profile", "openid"))
                        .build()

                    auth.startActivityForSignInWithProvider(activity, provider)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: ""
                            val emailUser = authResult.user?.email ?: ""
                            val displayName = authResult.user?.displayName ?: ""
                            handleOAuthLoginSuccess(
                                uid = uid,
                                emailUser = emailUser,
                                displayName = displayName,
                                provider = "google",
                                firestore = firestore,
                                participanteDao = participanteDao,
                                scope = scope,
                                navController = navController,
                                context = context,
                                onCargando = {
                                    estaCargandoGoogle = false
                                    estaCargandoMicrosoft = false
                                }
                            )
                        }
                        .addOnFailureListener { e ->
                            estaCargandoMicrosoft = false
                            Toast.makeText(
                                context,
                                "Error con Microsoft: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                enabled = !estaCargandoEmail && !estaCargandoGoogle && !estaCargandoMicrosoft,
                contentPadding = PaddingValues(8.dp)
            ) {
                if (estaCargandoMicrosoft) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.microsoft_logo),
                        contentDescription = "Microsoft",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Microsoft", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Si no tienes una cuenta, regístrate aquí",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedButton(
            onClick = {
                navController.navigate(
                    AppScreens.RegistroUsuario.createRoute("email", "", "", "")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }

    if (showDialogRecuperar) {
        DialogRecuperarContrasena(onDismiss = { showDialogRecuperar = false })
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
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

// Función reutilizable: tras cualquier login OAuth, comprobar si el usuario ya existe en Firestore
fun handleOAuthLoginSuccess(
    uid: String,
    emailUser: String,
    displayName: String,
    provider: String,
    firestore: FirebaseFirestore,
    participanteDao: ParticipanteDao,
    scope: CoroutineScope,
    navController: NavController,
    context: Context,
    onCargando: (Boolean) -> Unit
) {
    firestore.collection("participantes").document(uid).get()
        .addOnSuccessListener { doc ->
            onCargando(false)
            if (doc.exists()) {
                scope.launch {
                    participanteDao.insertar(
                        ParticipanteData(
                            idParticipante = uid,
                            nombre = doc.getString("nombre") ?: "",
                            apellidos = doc.getString("apellidos") ?: "",
                            emailEduca = doc.getString("emailEduca") ?: "",
                            centro = doc.getString("centro") ?: "",
                            codigoCentro = doc.getString("codigoCentro") ?: "",
                            rol = doc.getString("rol") ?: "participante",
                            fechaRegistro = doc.getString("fechaRegistro") ?: "",
                            idEvento = doc.getString("idEvento") ?: ""
                        )
                    )
                    navController.navigate(AppScreens.PantallaPrincipal.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(
                    AppScreens.RegistroUsuario.createRoute(
                        provider = provider,
                        uid = uid,
                        email = emailUser,
                        displayName = displayName
                    )
                )
            }
        }
        .addOnFailureListener { e ->
            onCargando(false)
            Toast.makeText(context, "Error verificando usuario: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
}