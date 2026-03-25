package com.example.ponenciapp.screens.utilidad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData

@Composable
fun IconoUsuario(participante: ParticipanteData) {
    var showCardDialog by remember { mutableStateOf(false) }
    val inicial = participante.nombre.firstOrNull()?.uppercase() ?: "U"

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            .clickable { showCardDialog = true }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = inicial,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }

    if (showCardDialog) {
        Dialog(onDismissRequest = { showCardDialog = false }) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.padding(16.dp).widthIn(min = 200.dp, max = 300.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = inicial,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${participante.nombre} ${participante.apellidos}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Email: ${participante.emailEduca}", style = MaterialTheme.typography.bodyMedium)
                            Text("Centro: ${participante.centro} - ${participante.codigoCentro}", style = MaterialTheme.typography.bodyMedium)
                            Text("Rol: ${participante.rol}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(modifier = Modifier.padding(bottom = 16.dp), onClick = { showCardDialog = false }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}