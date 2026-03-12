package com.example.ponenciapp.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ponenciapp.screens.Login
import com.example.ponenciapp.screens.PantallaPrincipal
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(destinoNotificacion: String? = null) {
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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppScreens.Login.route) {
            Login(navController)
        }
        composable(AppScreens.PantallaPrincipal.route) {
            BackHandler(true) {
                Toast.makeText(
                    context,
                    "Usa el botón de cerrar sesión para salir",
                    Toast.LENGTH_SHORT
                ).show()
            }
            PantallaPrincipal(navController)
        }
    }
}