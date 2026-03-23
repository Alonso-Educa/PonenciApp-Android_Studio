//package com.example.ponenciapp.screens
//
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.filled.EventNote
//import androidx.compose.material.icons.filled.Logout
//import androidx.compose.material.icons.filled.QrCodeScanner
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.NavigationBarItemDefaults
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.navigation.NavController
//import androidx.room.Room
//import com.example.ponenciapp.data.Estructura
//import com.example.ponenciapp.data.bbdd.AppDB
//import com.example.ponenciapp.data.bbdd.entities.EventoData
//import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
//import com.example.ponenciapp.navigation.AppScreens
//import com.example.ponenciapp.navigation.BottomNavItem
//import com.example.ponenciapp.screens.comun.Ajustes
//import com.example.ponenciapp.screens.organizador.MisEventos
//import com.example.ponenciapp.screens.participante.CheckInQR
//import com.example.ponenciapp.screens.participante.Ponencias
//import com.example.ponenciapp.screens.participante.Valoracion
//import com.google.firebase.Firebase
//import com.google.firebase.auth.auth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.launch

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PantallaPrincipal(navController: NavController) {
//
//    // Variables y funciones
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val auth = Firebase.auth
//    val uid = Firebase.auth.currentUser?.uid ?: ""
//
//    // Base de dtaos de room
//    val db = remember {
//        Room.databaseBuilder(
//            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
//        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
//    }
//
//    val participanteDao = db.participanteDao()
//
//    var participante by remember { mutableStateOf<ParticipanteData?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        // Carga el participante desde Room
//        participante = participanteDao.getParticipantePorId(uid)
//        // Si no tiene evento asignado y no es organizador le redirige a UnirseEvento
//        if (participante?.idEvento.isNullOrEmpty() && participante?.rol != "organizador") {
//            navController.navigate(AppScreens.UnirseEvento.route) {
//                popUpTo(0) { inclusive = true }
//            }
//        }
//        isLoading = false
//    }
//
//    // Si está cargando los datos muestra un iconito de carga
//    if (isLoading) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//        return
//    }
//
//    // BottomBar diferente según el rol del usuario (participante u organizador)
//    // Cuando se implemente el organizador en flutter quizá se quite de aquí
//    val bottomNavItems = when {
//        // Si es organizador solo ve Mis Eventos y Ajustes
//        participante?.rol == "organizador" -> listOf(
//            BottomNavItem("Mis Eventos", Icons.Default.Event, AppScreens.MisEventos.route),
//            BottomNavItem("Ajustes", Icons.Default.Settings, AppScreens.Ajustes.route)
//        )
//
//        // Si es participante ve todas las secciones
//        else -> listOf(
//            BottomNavItem("Check-in", Icons.Default.QrCodeScanner, AppScreens.CheckInQR.route),
//            BottomNavItem("Ponencias", Icons.Default.EventNote, AppScreens.Ponencias.route),
//            BottomNavItem("Valoración", Icons.Default.Star, AppScreens.Valoracion.route),
//            BottomNavItem("Ajustes", Icons.Default.Settings, AppScreens.Ajustes.route)
//        )
//    }
//
//    // La sección que se muestra al entrar es la primera del BottomBar
//    var seccionActual by remember {
//        mutableStateOf(bottomNavItems.first().route)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Column {
//                        Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                        Text(
//                            "Menú principal",
//                            fontSize = 12.sp,
//                            color = Color.White.copy(alpha = 0.8f)
//                        )
//                        // Nombre del participante debajo del título
//                        participante?.let {
//                            Text(
//                                "${it.nombre} ${it.apellidos}",
//                                fontSize = 12.sp,
//                                color = Color.White.copy(alpha = 0.8f)
//                            )
//                        }
//                    }
//                }, colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = Color.White
//                ), actions = {
//                    // Icono circular con la inicial del usuario
//                    // Al pulsar muestra un dialog con los datos del participante
//                    participante?.let { usuario ->
//                        var showCardDialog by remember { mutableStateOf(false) }
//                        val inicial = usuario.nombre.firstOrNull()?.uppercase() ?: "U"
//
//                        Box(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
//                                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
//                                .clickable { showCardDialog = true }) {
//                            Text(
//                                modifier = Modifier.align(Alignment.Center),
//                                text = inicial,
//                                style = MaterialTheme.typography.titleMedium,
//                                color = Color.White
//                            )
//                        }
//
//                        // Dialog con los datos del participante
//                        if (showCardDialog) {
//                            Dialog(onDismissRequest = { showCardDialog = false }) {
//                                Card(
//                                    shape = MaterialTheme.shapes.large,
//                                    colors = CardDefaults.cardColors(
//                                        containerColor = MaterialTheme.colorScheme.surface
//                                    ),
//                                    elevation = CardDefaults.cardElevation(8.dp),
//                                    modifier = Modifier.padding(10.dp)
//                                ) {
//                                    Column(
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        Row(
//                                            modifier = Modifier
//                                                .padding(16.dp)
//                                                .widthIn(min = 200.dp, max = 300.dp),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            Box(
//                                                modifier = Modifier
//                                                    .size(60.dp)
//                                                    .background(
//                                                        MaterialTheme.colorScheme.tertiary,
//                                                        CircleShape
//                                                    )
//                                                    .border(
//                                                        1.dp,
//                                                        MaterialTheme.colorScheme.secondary,
//                                                        CircleShape
//                                                    )
//                                            ) {
//                                                Text(
//                                                    modifier = Modifier.align(Alignment.Center),
//                                                    text = inicial,
//                                                    style = MaterialTheme.typography.titleMedium,
//                                                    color = Color.White
//                                                )
//                                            }
//                                            Column(modifier = Modifier.padding(16.dp)) {
//                                                Text(
//                                                    "${usuario.nombre} ${usuario.apellidos}",
//                                                    style = MaterialTheme.typography.titleMedium,
//                                                    fontWeight = FontWeight.Bold
//                                                )
//                                                Spacer(modifier = Modifier.height(8.dp))
//                                                Text(
//                                                    "Email: ${usuario.emailEduca}",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                                Text(
//                                                    "Centro: ${usuario.centro} - ${usuario.codigoCentro}",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                                Text(
//                                                    "Rol: ${usuario.rol}",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            }
//                                        }
//                                        Button(
//                                            modifier = Modifier.padding(bottom = 16.dp),
//                                            onClick = { showCardDialog = false }) {
//                                            Text("Cerrar")
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            )
//        },
//        // BottomBar con las pantallas según el rol
//        bottomBar = {
//            NavigationBar {
//                bottomNavItems.forEach { item ->
//                    NavigationBarItem(
//                        selected = seccionActual == item.route,
//                        onClick = { seccionActual = item.route },
//                        icon = { Icon(item.icon, contentDescription = item.label) },
//                        label = { Text(item.label) },
//                        colors = NavigationBarItemDefaults.colors(
//                            selectedIconColor = MaterialTheme.colorScheme.onTertiary,
//                            selectedTextColor = MaterialTheme.colorScheme.primary,
//                            indicatorColor = MaterialTheme.colorScheme.tertiary,
//                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        // Contenido central que cambia según la sección seleccionada en el BottomBar
//        // Mismo formato que la carga de widgets en flutter
//        Box(modifier = Modifier.padding(padding)) {
//            when (seccionActual) {
//                // Organizador
//                AppScreens.MisEventos.route -> MisEventos(navController)
//
//                // Participante
//                AppScreens.CheckInQR.route -> CheckInQR(
//                    idEvento = participante?.idEvento ?: "", idParticipante = uid
//                )
//
//                AppScreens.Ponencias.route -> Ponencias(
//                    navController = navController, idEvento = participante?.idEvento ?: ""
//                )
//
//                AppScreens.Valoracion.route -> Valoracion(
//                    idEvento = participante?.idEvento ?: "", idParticipante = uid
//                )
//                // Compartido por ambos
//                AppScreens.Ajustes.route -> Ajustes(
//                    participante = participante, onCerrarSesion = {
//                        scope.launch {
//                            auth.signOut()
//                            navController.navigate(AppScreens.Login.route) {
//                                popUpTo(0) { inclusive = true }
//                            }
//                        }
//                    }, navController
//                )
//            }
//        }
//    }
//}
//

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
    val participanteDao = db.participanteDao()

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val participante = participanteDao.getParticipantePorId(uid)
        when {
            // Organizador → Mis Eventos
            participante?.rol == "organizador" -> {
                navController.navigate(AppScreens.MisEventos.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            // Sin evento → Unirse a evento
            participante?.idEvento.isNullOrEmpty() -> {
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