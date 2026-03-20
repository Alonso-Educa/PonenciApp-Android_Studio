package com.example.ponenciapp.screens.comun

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.navigation.AppScreens
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@Composable
fun UnirseEvento(navController: NavController) {

    // Variables usadas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val uid = Firebase.auth.currentUser?.uid ?: ""

    // Base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val participanteDao = db.participanteDao()
    val eventoDao = db.eventoDao()

    // Para unirse a un evento escribiendo su codigo de evento
    var codigoEvento by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Interceptar botón atrás, así el usuario no puede salir sin unirse
    BackHandler(true) {
        Toast.makeText(
            context, "Debes unirte a un evento para continuar", Toast.LENGTH_SHORT
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Text(
            "Unirse a un evento", fontSize = 24.sp, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Introduce el código que te ha proporcionado el organizador del evento",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
        // campo de texto, cuando se escribe el texto se convierte automáticamente a mayúsculas
        OutlinedTextField(
            value = codigoEvento,
            onValueChange = { codigoEvento = it.uppercase() },
            label = { Text("Código del evento") },
            placeholder = { Text("Ej: FORM-X7K2-2026") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    // Validación de si el campo está vacío
                    codigoEvento.isBlank() -> Toast.makeText(
                        context, "Introduce el código del evento", Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        isLoading = true
                        // Busca en Firestore el evento con ese código
                        firestore.collection("eventos").whereEqualTo("codigoEvento", codigoEvento)
                            .get().addOnSuccessListener { result ->
                                // Si no encontró ningún evento con ese código
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

                                    // Guardar evento en Room
                                    scope.launch {
                                        eventoDao.insertar(
                                            EventoData(
                                                idEvento = idEvento,
                                                nombre = doc.getString("nombre") ?: "",
                                                fecha = doc.getString("fecha") ?: "",
                                                lugar = doc.getString("lugar") ?: "",
                                                descripcion = doc.getString("descripcion") ?: "",
                                                contrasena = doc.getString("contrasena") ?: "",
                                                codigoEvento = doc.getString("codigoEvento") ?: ""
                                            )
                                        )

                                        // Actualizar participante con idEvento en Room
                                        val participanteActual =
                                            participanteDao.getParticipantePorId(uid)
                                        participanteActual?.let {
                                            participanteDao.actualizar(
                                                it.copy(idEvento = idEvento)
                                            )
                                        }

                                        // Actualizar participante con idEvento en Firestore
                                        firestore.collection("participantes").document(uid)
                                            .update("idEvento", idEvento).addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Te has unido al evento correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
//                                                navController.navigate(AppScreens.PantallaPrincipal.route) {
//                                                    popUpTo(0) { inclusive = true }
//                                                }
                                            }.addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Error al unirse al evento: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        // Añadir el participante a la lista de participantes del evento
                                        firestore.collection("eventos").document(idEvento).set(
                                                mapOf("participantes" to FieldValue.arrayUnion(uid)),
                                                SetOptions.merge()
                                            ).addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Te has unido al evento correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Limpia la pila de navegación para que el usuario no pueda volver atrás
                                                navController.navigate(AppScreens.PantallaPrincipal.route) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }.addOnFailureListener { e ->
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
                            // Si falla al buscar en Firestore
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
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), enabled = !isLoading
        ) {
            // Muestra un iconito de carga cuando se procesa la solicitud
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White, modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Unirse al evento", fontSize = 16.sp)
            }
        }

        // Botón cerrar sesión para comodidad
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate(AppScreens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }) {
            Text("Cerrar sesión", color = Color.Gray)
        }
    }
}