package com.example.ponenciapp.screens.comun

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.screens.utilidad.IconoUsuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnirseEvento(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = Firebase.auth.currentUser?.uid ?: ""

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val participanteDao = db.participanteDao()
    val eventoDao = db.eventoDao()

    var participante by remember { mutableStateOf<ParticipanteData?>(null) }
    var codigoEvento by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Interceptar botón atrás — el usuario no puede salir sin unirse
    BackHandler(true) {
        Toast.makeText(context, "Debes unirte a un evento para continuar", Toast.LENGTH_SHORT)
            .show()
    }

    LaunchedEffect(Unit) {
        // Carga el participante desde Room
        participante = participanteDao.getParticipantePorId(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Unirse a un evento",
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
            // Muestra BottomBar con Unirse + Ajustes
            BottomBarUnirseEvento(
                navController = navController,
                rutaActual = AppScreens.UnirseEvento.route
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Unirse a un evento", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Introduce el código que te ha proporcionado el organizador del evento",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de texto — el texto se convierte automáticamente a mayúsculas
            OutlinedTextField(
                value = codigoEvento,
                onValueChange = {
                    codigoEvento = it.filterNot { char -> char.isWhitespace() }
                },
                label = { Text("Código del evento") },
                placeholder = { Text("Ej: FORM-X7K2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        codigoEvento.isBlank() -> Toast.makeText(
                            context, "Introduce el código del evento", Toast.LENGTH_SHORT
                        ).show()

                        else -> {
                            isLoading = true
                            // Busca en Firestore el evento con ese código
                            firestore.collection("eventos")
                                .whereEqualTo("codigoEvento", codigoEvento)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Código de evento no encontrado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val doc = result.documents.first()
                                        val idEvento = doc.id

                                        scope.launch {
                                            // 1. Guardar el evento en Room
                                            eventoDao.insertar(
                                                EventoData(
                                                    idEvento = idEvento,
                                                    nombre = doc.getString("nombre") ?: "",
                                                    fecha = doc.getString("fecha") ?: "",
                                                    lugar = doc.getString("lugar") ?: "",
                                                    descripcion = doc.getString("descripcion")
                                                        ?: "",
                                                    codigoEvento = doc.getString("codigoEvento")
                                                        ?: "",
                                                    idOrganizador = doc.getString("idOrganizador")
                                                        ?: ""
                                                )
                                            )

                                            // 2. Actualizar participante en Room
                                            val participanteActual =
                                                participanteDao.getParticipantePorId(uid)
                                            participanteActual?.let {
                                                participanteDao.actualizar(it.copy(idEvento = idEvento))
                                            }

                                            // 3. Actualizar participante en Firestore
                                            firestore.collection("participantes").document(uid)
                                                .update("idEvento", idEvento)

                                            // 4. Añadir uid al array de participantes del evento
                                            firestore.collection("eventos").document(idEvento)
                                                .set(
                                                    mapOf(
                                                        "participantes" to FieldValue.arrayUnion(
                                                            uid
                                                        )
                                                    ),
                                                    SetOptions.merge()
                                                )
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Te has unido al evento correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Limpia la pila y navega a CheckInQR
                                                    navController.navigate(AppScreens.CheckInQR.route) {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Error al unirse al evento: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Error buscando el evento: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                    Text("Unirse al evento", fontSize = 16.sp)
                }
            }

            // Botón cerrar sesión por si se equivocó de cuenta
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                Firebase.auth.signOut()
                navController.navigate(AppScreens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}