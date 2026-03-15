package com.example.ponenciapp.navigation

sealed class AppScreens(val route: String) {
    object Login : AppScreens("Login")
    object PantallaPrincipal : AppScreens("PantallaPrincipal")
    object UnirseEvento : AppScreens("UnirseEvento")
    object CheckInQR : AppScreens("CheckInQR")
    object Ponencias : AppScreens("Ponencias")
    object Valoracion : AppScreens("Valoracion")
    object Ajustes : AppScreens("Ajustes")
    object MisEventos : AppScreens("MisEventos")
    object DetalleEvento : AppScreens("DetalleEvento/{idEvento}") {
        fun createRoute(idEvento: String) = "DetalleEvento/$idEvento"
    }
    object DetallePonencia : AppScreens("DetallePonencia/{idPonencia}") {
        fun createRoute(idPonencia: String) = "DetallePonencia/$idPonencia"
    }

    companion object {
        fun fromRoute(route: String): AppScreens? {
            return when (route.substringBefore("/")) {
                "Login" -> Login
                "PantallaPrincipal" -> PantallaPrincipal
                "UnirseEvento" -> UnirseEvento
                "CheckInQR" -> CheckInQR
                "Ponencias" -> Ponencias
                "Valoracion" -> Valoracion
                "Ajustes" -> Ajustes
                "MisEventos" -> MisEventos
                "DetalleEvento" -> DetalleEvento
                "DetallePonencia" -> DetallePonencia
                else -> null
            }
        }
    }
}