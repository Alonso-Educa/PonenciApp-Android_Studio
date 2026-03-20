package com.example.ponenciapp.screens.participante

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.AsistenciaData
import com.example.ponenciapp.notification.NotificationHandler
import com.example.ponenciapp.screens.comun.EscanerQR
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckInQR(idEvento: String, idParticipante: String) {

    // Variables usadas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val notificationHandler = NotificationHandler(context)

    // base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    val asistenciaDao = db.asistenciaDao()

    var checkInRealizado by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var escaneando by remember { mutableStateOf(false) }

    // Comprobar si ya hizo check-in
    LaunchedEffect(Unit) {
        firestore.collection("asistencias")
            .whereEqualTo("idParticipante", idParticipante)
            .whereEqualTo("idEvento", idEvento)
            .whereEqualTo("tipo", "checkin")
            .get()
            .addOnSuccessListener { result ->
                checkInRealizado = !result.isEmpty
                isLoading = false
            }
            .addOnFailureListener {
                scope.launch {
                    checkInRealizado = asistenciaDao.getCheckIn(idParticipante) != null
                    isLoading = false
                }
            }
    }

    // Si está cargando muestra el iconito de carga
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Pantalla del escáner
    if (escaneando) {
        EscanerQR(
            onQRLeido = { valor ->
                // Si el qr devuelve el código correcto se pasa a firebase y se crea la asistencia
                if (valor == "checkin:$idEvento") {
                    val idAsistencia = "${idParticipante}_${idEvento}_checkin"
                    firestore.collection("asistencias").document(idAsistencia)
                        .set(mapOf(
                            "idAsistencia" to idAsistencia,
                            "idParticipante" to idParticipante,
                            "idEvento" to idEvento,
                            "idPonencia" to "",
                            "tipo" to "checkin",
                            "fechaHora" to formatearFechaHora() // TODO
                        ))
                        .addOnSuccessListener {
                            scope.launch {
                                asistenciaDao.insertar(
                                    AsistenciaData(
                                        idAsistencia = idAsistencia,
                                        idParticipante = idParticipante,
                                        idEvento = idEvento,
                                        idPonencia = "",
                                        tipo = "checkin",
                                        fechaHora = formatearFechaHora()
                                    )
                                )
                                checkInRealizado = true
                                escaneando = false
                                Toast.makeText(
                                    context,
                                    "¡Check-in realizado correctamente!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            escaneando = false
                            Toast.makeText(
                                context,
                                "Error al registrar: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    escaneando = false
                    Toast.makeText(
                        context,
                        "QR no válido para este evento",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onCancelar = { escaneando = false }
        )
        return
    }

    // Pantalla principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Si se ha realizado el checkin muestra el mensaje
        if (checkInRealizado) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡Check-in realizado!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tu asistencia al evento ha sido registrada correctamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
//            Row(
//                modifier = Modifier.padding(top = 32.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Button(
//                    onClick = {
//                        notificationHandler.enviarNotificacionConDestino("Notificacion a otra ventana", "prueba", "Valoracion")
//                    },
//                ){
//                    Text("Notificacion a otra ventana")
//                }
//                Button(
//                    onClick = {
//                        notificationHandler.enviarNotificacionSimple("Notificacion a otra ventana", "prueba")
//                    },
//                ){
//                    Text("Notificacion simple")
//                }
//            }
            // Si no se ha realizado el checkin le pide al usuario que escanee el qr
        } else {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Check-in al evento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Escanea el código QR del evento para registrar tu asistencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Botón para escanear el qr
            Button(
                onClick = { escaneando = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR")
            }
        }
    }
}

fun formatearFechaHora(): String {
    val sdfCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdfCompleto.format(Date())
}