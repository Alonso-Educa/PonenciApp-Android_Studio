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
    object DetallePonenciaParticipante : AppScreens("DetallePonenciaParticipante/{idPonencia}") {
        fun createRoute(idPonencia: String) = "DetallePonenciaParticipante/$idPonencia"
    }
    object DetallePonenciaOrganizador : AppScreens("DetallePonenciaOrganizador/{idPonencia}") {
        fun createRoute(idPonencia: String) = "DetallePonenciaOrganizador/$idPonencia"
    }
    object ChatbotAsistente : AppScreens("ChatbotAsistente")

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
                "DetallePonenciaParticipante" -> DetallePonenciaParticipante
                "DetallePonenciaOrganizador" -> DetallePonenciaOrganizador
                "ChatbotAsistente" -> ChatbotAsistente
                else -> null
            }
        }
    }
}