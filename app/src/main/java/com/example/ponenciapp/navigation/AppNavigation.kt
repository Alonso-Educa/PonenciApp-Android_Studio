package com.example.ponenciapp.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ponenciapp.screens.*
import com.example.ponenciapp.screens.comun.*
import com.example.ponenciapp.screens.organizador.*
import com.example.ponenciapp.screens.participante.*
import com.example.ponenciapp.screens.utilidad.ThemeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(destinoNotificacion: String? = null, themeViewModel: ThemeViewModel) {
    val context = LocalContext.current
    val auth = Firebase.auth

    val startDestination = if (auth.currentUser != null) {
        AppScreens.PantallaPrincipal.route
    } else {
        AppScreens.Login.route
    }

    val navController = rememberNavController()

    LaunchedEffect(destinoNotificacion) {
        if (destinoNotificacion != null && AppScreens.fromRoute(destinoNotificacion) != null) {
            navController.navigate(destinoNotificacion) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppScreens.Login.route) {
            Login(navController, themeViewModel)
        }
        // Router — no tiene UI propia, solo redirige
        composable(AppScreens.PantallaPrincipal.route) {
            PantallaPrincipal(navController)
        }
        // Pantallas con BackHandler
        composable(AppScreens.UnirseEvento.route) {
            BackHandler(true) {
                Toast.makeText(context, "Debes unirte a un evento para continuar", Toast.LENGTH_SHORT).show()
            }
            UnirseEvento(navController)
        }
        composable(AppScreens.CheckInQR.route) {
            BackHandler(true) {
                Toast.makeText(context, "Usa el botón de cerrar sesión para salir", Toast.LENGTH_SHORT).show()
            }
            CheckInQR(navController)
        }
        composable(AppScreens.Ponencias.route) {
            BackHandler(true) {
                Toast.makeText(context, "Usa el botón de cerrar sesión para salir", Toast.LENGTH_SHORT).show()
            }
            MisPonencias(navController)
        }
        composable(AppScreens.Valoracion.route) {
            BackHandler(true) {
                Toast.makeText(context, "Usa el botón de cerrar sesión para salir", Toast.LENGTH_SHORT).show()
            }
            Valoracion(navController)
        }
        composable(AppScreens.Ajustes.route) {
            BackHandler(true) {
                Toast.makeText(context, "Usa el botón de cerrar sesión para salir", Toast.LENGTH_SHORT).show()
            }
            Ajustes(navController, themeViewModel)
        }
        composable(AppScreens.MisEventos.route) {
            BackHandler(true) {
                Toast.makeText(context, "Usa el botón de cerrar sesión para salir", Toast.LENGTH_SHORT).show()
            }
            MisEventos(navController)
        }
        composable(AppScreens.ChatbotAsistente.route) {
            BackHandler(true) {
                navController.navigate("Ajustes") {
                    popUpTo(AppScreens.ChatbotAsistente.route) {
                        inclusive = true
                    }
                }
            }
            ChatbotAsistente(navController)
        }
        composable(
            route = AppScreens.DetalleEvento.route,
            arguments = listOf(navArgument("idEvento") { type = NavType.StringType })
        ) { backStackEntry ->
            val idEvento = backStackEntry.arguments?.getString("idEvento") ?: ""
            DetalleEvento(navController, idEvento)
        }
        composable(
            route = AppScreens.DetallePonenciaParticipante.route,
            arguments = listOf(navArgument("idPonencia") { type = NavType.StringType })
        ) { backStackEntry ->
            val idPonencia = backStackEntry.arguments?.getString("idPonencia") ?: ""
            DetallePonenciaParticipante(navController, idPonencia)
        }
        composable(
            route = AppScreens.DetallePonenciaOrganizador.route,
            arguments = listOf(navArgument("idPonencia") { type = NavType.StringType })
        ) { backStackEntry ->
            val idPonencia = backStackEntry.arguments?.getString("idPonencia") ?: ""
            DetallePonenciaOrganizador(navController, idPonencia)
        }
        composable(
            route = AppScreens.RegistroParticipante.route,
            arguments = listOf(
                navArgument("provider") { type = NavType.StringType },
                navArgument("uid") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            RegistroParticipante(
                navController = navController,
                proveedor = backStackEntry.arguments?.getString("provider") ?: "email",
                uid = backStackEntry.arguments?.getString("uid") ?: "",
                emailExterno = backStackEntry.arguments?.getString("email") ?: "",
                displayName = backStackEntry.arguments?.getString("displayName") ?: "",
                fotoUrlExterno = backStackEntry.arguments?.getString("fotoUrl") ?: ""
            )
        }
    }
}