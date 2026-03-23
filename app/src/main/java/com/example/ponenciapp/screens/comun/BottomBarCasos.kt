package com.example.ponenciapp.screens.comun

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.ponenciapp.navigation.AppScreens
import com.example.ponenciapp.navigation.BottomNavItem

@Composable
fun BottomBarParticipante(navController: NavController, rutaActual: String) {
    val items = listOf(
        BottomNavItem("Check-in", Icons.Default.QrCodeScanner, AppScreens.CheckInQR.route),
        BottomNavItem("Ponencias", Icons.Default.EventNote, AppScreens.Ponencias.route),
        BottomNavItem("Valoración", Icons.Default.Star, AppScreens.Valoracion.route),
        BottomNavItem("Ajustes", Icons.Default.Settings, AppScreens.Ajustes.route)
    )
    BottomBarComun(navController, rutaActual, items)
}

@Composable
fun BottomBarOrganizador(navController: NavController, rutaActual: String) {
    val items = listOf(
        BottomNavItem("Mis Eventos", Icons.Default.Event, AppScreens.MisEventos.route),
        BottomNavItem("Ajustes", Icons.Default.Settings, AppScreens.Ajustes.route)
    )
    BottomBarComun(navController, rutaActual, items)
}

@Composable
fun BottomBarUnirseEvento(navController: NavController, rutaActual: String) {
    val items = listOf(
        BottomNavItem("Unirse Evento", Icons.Default.QrCode, AppScreens.UnirseEvento.route),
        BottomNavItem("Ajustes", Icons.Default.Settings, AppScreens.Ajustes.route)
    )
    BottomBarComun(navController, rutaActual, items)
}

@Composable
private fun BottomBarComun(
    navController: NavController,
    rutaActual: String,
    items: List<BottomNavItem>
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = rutaActual == item.route,
                onClick = {
                    if (rutaActual != item.route) {
                        navController.navigate(item.route) {
                            // Evita acumular pantallas en la pila al cambiar de tab
                            popUpTo(AppScreens.CheckInQR.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onTertiary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.tertiary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}