package com.example.ponenciapp.screens.participante

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.ValoracionData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun Valoracion(idEvento: String, idParticipante: String) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val valoracionDao = db.valoracionDao()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Valoración general del evento
    var puntuacionEvento by remember { mutableIntStateOf(0) }
    var comentarioEvento by remember { mutableStateOf("") }

    // Si ya existe valoración previa, la carga
    LaunchedEffect(Unit) {
        firestore.collection("valoraciones").whereEqualTo("idParticipante", idParticipante)
            .whereEqualTo("idEvento", idEvento).whereEqualTo("tipo", "evento").get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    puntuacionEvento = doc.getLong("puntuacion")?.toInt() ?: 0
                    comentarioEvento = doc.getString("comentario") ?: ""
                }
                isLoading = false  // ← añadir aquí
            }.addOnFailureListener {
                isLoading = false  // ← añadir aquí
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Valoración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Valoración general del evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "¿Cómo valorarías el evento en general?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                SelectorEstrellas(
                    puntuacion = puntuacionEvento, onPuntuacionChange = { puntuacionEvento = it })
                OutlinedTextField(
                    value = comentarioEvento,
                    onValueChange = { comentarioEvento = it },
                    label = { Text("Comentario (opcional)") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                if (puntuacionEvento == 0) {
                    Toast.makeText(
                        context, "Introduce una puntuación del evento", Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                isSaving = true
                val idValoracion = "${idParticipante}_${idEvento}_evento"
                firestore.collection("valoraciones").document(idValoracion).set(
                        mapOf(
                            "idValoracion" to idValoracion,
                            "idParticipante" to idParticipante,
                            "idEvento" to idEvento,
                            "idPonencia" to "",
                            "tipo" to "evento",
                            "puntuacion" to puntuacionEvento,
                            "comentario" to comentarioEvento
                        )
                    ).addOnSuccessListener {
                        scope.launch {
                            valoracionDao.insertar(
                                ValoracionData(
                                    idValoracion = idValoracion,
                                    idParticipante = idParticipante,
                                    idPonencia = "",
                                    tipo = "evento",
                                    puntuacion = puntuacionEvento,
                                    comentario = comentarioEvento
                                )
                            )
                            isSaving = false
                            Toast.makeText(
                                context, "Valoración guardada correctamente", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener { e ->
                        isSaving = false
                        Toast.makeText(
                            context, "Error guardando valoración: ${e.message}", Toast.LENGTH_SHORT
                        ).show()
                    }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar valoración")
            }
        }
    }
}

// Selector de estrellas
@Composable
fun SelectorEstrellas(
    puntuacion: Int, onPuntuacionChange: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { estrella ->
            IconButton(
                onClick = { onPuntuacionChange(estrella) }, modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (estrella <= puntuacion) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Estrella $estrella",
                    tint = if (estrella <= puntuacion) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}