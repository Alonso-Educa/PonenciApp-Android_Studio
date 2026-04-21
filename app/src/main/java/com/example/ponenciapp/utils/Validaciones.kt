package com.example.ponenciapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilidades de validación centralizadas para PonenciApp.
 *
 * Esta clase agrupa la lógica de validación que originalmente se encontraba
 * dispersa en los Composables (Login, RegistroParticipante, Ajustes,
 * DetalleEvento, MisEventos, CheckInQR), con el objetivo de facilitar su
 * reutilización y la creación de tests unitarios de caja blanca.
 */
object Validaciones {

    private val REGEX_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_CODIGO_EVENTO = Regex("^FORM-[A-Za-z0-9]{4}$")
    private const val CARACTERES_CODIGO =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    /** Valida el formato de un email. Devuelve false si está vacío o no coincide con la regex. */
    fun emailEsValido(email: String): Boolean {
        if (email.isEmpty()) return false
        return REGEX_EMAIL.matches(email)
    }

    /**
     * Validación de contraseña utilizada en la pantalla de login.
     * - Vacía → "Introduce tu contraseña"
     * - Menos de 6 caracteres → "La contraseña debe tener al menos 6 caracteres"
     * - Correcta → null
     */
    fun validarPasswordLogin(password: String): String? {
        if (password.isEmpty()) return "Introduce tu contraseña"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    /**
     * Validación de contraseña utilizada en el registro de organizador.
     * - Vacía/null → "Campo obligatorio"
     * - Menos de 10 caracteres → "Mínimo 10 caracteres"
     * - Sin carácter especial → "Debe incluir al menos un carácter especial"
     * - Correcta → null
     */
    fun validarPasswordRegistro(value: String?): String? {
        val texto = value ?: ""
        if (texto.isEmpty()) return "Campo obligatorio"
        if (texto.length < 10) return "Mínimo 10 caracteres"
        if (!texto.any { !it.isLetterOrDigit() }) {
            return "Debe incluir al menos un carácter especial"
        }
        return null
    }

    /**
     * Validación de contraseña utilizada en el registro de participante.
     * - Vacía/null → "Campo obligatorio"
     * - Menos de 6 caracteres → "Mínimo 6 caracteres"
     * - Correcta → null
     */
    fun validarPasswordParticipante(value: String?): String? {
        val texto = value ?: ""
        if (texto.isEmpty()) return "Campo obligatorio"
        if (texto.length < 6) return "Mínimo 6 caracteres"
        return null
    }

    /** Valida que un campo de texto no esté vacío. */
    fun validarCampoObligatorio(value: String?): String? {
        val texto = value ?: ""
        if (texto.isEmpty()) return "Campo obligatorio"
        return null
    }

    /** Comprueba que la hora de fin sea estrictamente posterior a la de inicio. */
    fun horaFinEsValida(horaInicio: String, horaFin: String): Boolean {
        return horaFin.trim().compareTo(horaInicio.trim()) > 0
    }

    /** Genera un código de evento con formato "FORM-XXXX". */
    fun generarCodigoEvento(): String {
        val random = java.security.SecureRandom()
        val sb = StringBuilder("FORM-")
        repeat(4) {
            sb.append(CARACTERES_CODIGO[random.nextInt(CARACTERES_CODIGO.length)])
        }
        return sb.toString()
    }

    /** Comprueba que un código de evento sigue el formato "FORM-XXXX". */
    fun codigoEventoEsValido(codigo: String): Boolean {
        return REGEX_CODIGO_EVENTO.matches(codigo)
    }

    /** Formatea una fecha concreta como "dd/MM/yyyy HH:mm:ss". */
    fun formatearFechaHora(date: Date): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formato.format(date)
    }

    /** Formatea la fecha y hora actuales como "dd/MM/yyyy HH:mm:ss". */
    fun formatearFechaHora(): String {
        return formatearFechaHora(Date())
    }

    /**
     * Parsea una fecha en formato "dd/MM/yyyy".
     * Devuelve null si el formato es incorrecto (por ejemplo, separador distinto
     * de '/' o número de componentes inválido).
     */
    fun parsearFechaEvento(fecha: String): Date? {
        val partes = fecha.split('/')
        if (partes.size != 3) return null
        val dia = partes[0].toIntOrNull() ?: return null
        val mes = partes[1].toIntOrNull() ?: return null
        val anio = partes[2].toIntOrNull() ?: return null
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(anio, mes - 1, dia, 0, 0, 0)
        return calendar.time
    }

    /** Convierte un timestamp en milisegundos a una fecha con formato "dd/MM/yyyy". */
    fun convertMillisToDate(millis: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date(millis))
    }
}
