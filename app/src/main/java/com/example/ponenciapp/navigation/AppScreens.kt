package com.example.ponenciapp.navigation

sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object PantallaPrincipal : AppScreens("pantalla_principal")
    object CheckInQR : AppScreens("checkin_qr")
    object AsistenciaQR : AppScreens("asistencia_qr/{idPonencia}") {
        fun createRoute(idPonencia: String) = "asistencia_qr/$idPonencia"
    }
    object Ponencias : AppScreens("ponencias")
    object DetallePonencia : AppScreens("detalle_ponencia/{idPonencia}") {
        fun createRoute(idPonencia: String) = "detalle_ponencia/$idPonencia"
    }
    object Valoracion : AppScreens("valoracion")
    object Ajustes : AppScreens("ajustes")

    companion object {
        fun fromRoute(route: String): AppScreens? {
            return when (route.substringBefore("/")) {
                "login" -> Login
                "pantalla_principal" -> PantallaPrincipal
                else -> null
            }
        }
    }
}