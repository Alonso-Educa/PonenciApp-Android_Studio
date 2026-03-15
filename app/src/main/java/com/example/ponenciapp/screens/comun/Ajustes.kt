package com.example.ponenciapp.screens.comun

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData

@Composable
fun Ajustes(
    participante: ParticipanteData?,
    onCerrarSesion: () -> Unit
) {
    // Para ir a la web
    // Se vinculará la página de ayuda de la parte de flutter cuando se realice
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Ajustes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Tarjeta con los datos del participante
        participante?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mi perfil", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nombre: ${it.nombre} ${it.apellidos}")
                    Text("Correo: ${it.emailEduca}", color = Color.Gray)
                    Text("Centro: ${it.centro} - ${it.codigoCentro}", color = Color.Gray)
                    Text("Rol de usuario: ${it.rol}", color = Color.Gray)
                }
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Abrir la página de ayuda (aún no está hecha)
            TextButton(
                onClick = { context.startActivity(intent) }
            ) {
                Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = "Ayuda")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ayuda")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ArrowOutward, contentDescription = "Salir")
            }

            // Cerrar sesión
            TextButton(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}