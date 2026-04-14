package com.example.ponenciapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.navigation.AppScreens
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun PantallaPrincipal(navController: NavController) {
    val context = LocalContext.current
    val uid = Firebase.auth.currentUser?.uid ?: ""

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }
    val usuarioDao = db.usuarioDao()

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val usuario = usuarioDao.getParticipantePorId(uid)
        when {
            // Organizador → Mis Eventos
            usuario?.rol == "organizador" -> {
                navController.navigate(AppScreens.MisEventos.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            // Participante sin evento → Unirse a evento
            usuario?.idEvento.isNullOrEmpty() -> {
                navController.navigate(AppScreens.UnirseEvento.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            // Participante con evento → Check-in
            else -> {
                navController.navigate(AppScreens.CheckInQR.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        isLoading = false
    }

    // Muestra spinner mientras decide a dónde navegar
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}