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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData

// Función para mostrar el icono de usuario (participante/organizador)
@Composable
fun IconoUsuario(usuario: ParticipanteData) {

    var showCardDialog by remember { mutableStateOf(false) }
    val inicial = usuario.nombre.firstOrNull()?.uppercase() ?: "U"

    // Imagen de perfil si la hay
    if (!usuario.fotoPerfilUrl.isNullOrEmpty()) {
        AsyncImage(
            model = usuario.fotoPerfilUrl,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { showCardDialog = true },
            contentScale = ContentScale.Crop
        )
    } else {
        // Inicial si no hay foto
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
    }

    // Mostrar el diálogo con la tarjeta de usuario
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
                        // Imagen de perfil si la hay
                        if (!usuario.fotoPerfilUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = usuario.fotoPerfilUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable { showCardDialog = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Inicial si no hay foto
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
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
                        }
                        // Datos del usuario
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${usuario.nombre} ${usuario.apellidos}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Email: ${usuario.emailEduca}", style = MaterialTheme.typography.bodyMedium)
                            Text("Centro: ${usuario.centro} - ${usuario.codigoCentro}", style = MaterialTheme.typography.bodyMedium)
                            Text("Rol: ${usuario.rol.replaceFirstChar { c -> c.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
                        }
                        //it.rol.replaceFirstChar { c -> c.uppercase() }
                    }
                    Button(modifier = Modifier.padding(bottom = 16.dp), onClick = { showCardDialog = false }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}