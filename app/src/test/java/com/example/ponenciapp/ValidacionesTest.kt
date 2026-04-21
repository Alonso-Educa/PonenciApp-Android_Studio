package com.example.ponenciapp

import com.example.ponenciapp.utils.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Tests unitarios de caja blanca para [com.example.ponenciapp.utils.Validaciones].
 *
 * Tabla de casos de prueba
 * ------------------------
 * GRUPO 1 — emailEsValido()
 *   CB01  ""                        → false
 *   CB02  "usuariosinarroba.com"    → false
 *   CB03  "usuario@"                → false
 *   CB04  "user@educa.junta.es"     → true
 *
 * GRUPO 2 — validarPasswordLogin() + validarPasswordParticipante()
 *   CB05  ""        → "Introduce tu contraseña"
 *   CB06  "abcde"   → "La contraseña debe tener al menos 6 caracteres"
 *   CB07  "abcdef"  → null
 *   CB08  validarPasswordParticipante("") → "Campo obligatorio"
 *
 * GRUPO 3 — validarPasswordRegistro()
 *   CB09  ""             → "Campo obligatorio"
 *   CB10  "abcde!@#1"    → "Mínimo 10 caracteres"
 *   CB11  "abcdefghij"   → "Debe incluir al menos un carácter especial"
 *   CB12  "abcde!@#1X"   → null
 *
 * GRUPO 4 — validarCampoObligatorio()
 *   CB13  ""           → "Campo obligatorio"
 *   CB14  null         → "Campo obligatorio"
 *   CB15  "Ana García" → null
 *
 * GRUPO 5 — horaFinEsValida()
 *   CB16  ("10:00", "10:00") → false
 *   CB17  ("17:00", "09:00") → false
 *   CB18  ("09:00", "17:30") → true
 *
 * GRUPO 6 — generarCodigoEvento() + codigoEventoEsValido()
 *   CB19  50 códigos generados pasan codigoEventoEsValido()
 *   CB20  "FORM-Ab1Z" → true
 *   CB21  "AB12"      → false
 *   CB22  "FORM-AB1!" → false
 *
 * GRUPO 7 — formatearFechaHora() + parsearFechaEvento()
 *   CB23  Calendar(2025,0,5,8,3,7)  → "05/01/2025 08:03:07"
 *   CB24  Calendar(2025,0,1,0,0,0)  → "01/01/2025 00:00:00"
 *   CB25  parsearFechaEvento("25/12/2025") → fecha con día=25, mes=12, año=2025
 *   CB26  parsearFechaEvento("25-12-2025") → null
 */
class ValidacionesTest {

    // ---------------------------------------------------------------------
    // GRUPO 1 — emailEsValido()
    // ---------------------------------------------------------------------

    @Test
    fun cb01_emailVacioDevuelveFalse() {
        assertFalse(Validaciones.emailEsValido(""))
    }

    @Test
    fun cb02_emailSinArrobaDevuelveFalse() {
        assertFalse(Validaciones.emailEsValido("usuariosinarroba.com"))
    }

    @Test
    fun cb03_emailSinDominioDevuelveFalse() {
        assertFalse(Validaciones.emailEsValido("usuario@"))
    }

    @Test
    fun cb04_emailValidoDevuelveTrue() {
        assertTrue(Validaciones.emailEsValido("user@educa.junta.es"))
    }

    // ---------------------------------------------------------------------
    // GRUPO 2 — validarPasswordLogin() + validarPasswordParticipante()
    // ---------------------------------------------------------------------

    @Test
    fun cb05_passwordLoginVaciaDevuelveMensaje() {
        assertEquals("Introduce tu contraseña", Validaciones.validarPasswordLogin(""))
    }

    @Test
    fun cb06_passwordLoginCortaDevuelveMensaje() {
        assertEquals(
            "La contraseña debe tener al menos 6 caracteres",
            Validaciones.validarPasswordLogin("abcde")
        )
    }

    @Test
    fun cb07_passwordLoginValidaDevuelveNull() {
        assertNull(Validaciones.validarPasswordLogin("abcdef"))
    }

    @Test
    fun cb08_passwordParticipanteVaciaDevuelveMensaje() {
        assertEquals("Campo obligatorio", Validaciones.validarPasswordParticipante(""))
    }

    // ---------------------------------------------------------------------
    // GRUPO 3 — validarPasswordRegistro()
    // ---------------------------------------------------------------------

    @Test
    fun cb09_passwordRegistroVaciaDevuelveCampoObligatorio() {
        assertEquals("Campo obligatorio", Validaciones.validarPasswordRegistro(""))
    }

    @Test
    fun cb10_passwordRegistroCortaDevuelveMinimo10() {
        assertEquals("Mínimo 10 caracteres", Validaciones.validarPasswordRegistro("abcde!@#1"))
    }

    @Test
    fun cb11_passwordRegistroSinEspecialDevuelveMensaje() {
        assertEquals(
            "Debe incluir al menos un carácter especial",
            Validaciones.validarPasswordRegistro("abcdefghij")
        )
    }

    @Test
    fun cb12_passwordRegistroValidaDevuelveNull() {
        assertNull(Validaciones.validarPasswordRegistro("abcde!@#1X"))
    }

    // ---------------------------------------------------------------------
    // GRUPO 4 — validarCampoObligatorio()
    // ---------------------------------------------------------------------

    @Test
    fun cb13_campoObligatorioVacioDevuelveMensaje() {
        assertEquals("Campo obligatorio", Validaciones.validarCampoObligatorio(""))
    }

    @Test
    fun cb14_campoObligatorioNullDevuelveMensaje() {
        assertEquals("Campo obligatorio", Validaciones.validarCampoObligatorio(null))
    }

    @Test
    fun cb15_campoObligatorioConTextoDevuelveNull() {
        assertNull(Validaciones.validarCampoObligatorio("Ana García"))
    }

    // ---------------------------------------------------------------------
    // GRUPO 5 — horaFinEsValida()
    // ---------------------------------------------------------------------

    @Test
    fun cb16_horasIgualesDevuelveFalse() {
        assertFalse(Validaciones.horaFinEsValida("10:00", "10:00"))
    }

    @Test
    fun cb17_horaFinAnteriorDevuelveFalse() {
        assertFalse(Validaciones.horaFinEsValida("17:00", "09:00"))
    }

    @Test
    fun cb18_horaFinPosteriorDevuelveTrue() {
        assertTrue(Validaciones.horaFinEsValida("09:00", "17:30"))
    }

    // ---------------------------------------------------------------------
    // GRUPO 6 — generarCodigoEvento() + codigoEventoEsValido()
    // ---------------------------------------------------------------------

    @Test
    fun cb19_generar50CodigosTodosValidos() {
        repeat(50) {
            val codigo = Validaciones.generarCodigoEvento()
            assertTrue(
                "Código generado no cumple el formato esperado: $codigo",
                Validaciones.codigoEventoEsValido(codigo)
            )
        }
    }

    @Test
    fun cb20_codigoEventoValidoDevuelveTrue() {
        assertTrue(Validaciones.codigoEventoEsValido("FORM-Ab1Z"))
    }

    @Test
    fun cb21_codigoEventoSinPrefijoDevuelveFalse() {
        assertFalse(Validaciones.codigoEventoEsValido("AB12"))
    }

    @Test
    fun cb22_codigoEventoConCaracterInvalidoDevuelveFalse() {
        assertFalse(Validaciones.codigoEventoEsValido("FORM-AB1!"))
    }

    // ---------------------------------------------------------------------
    // GRUPO 7 — formatearFechaHora() + parsearFechaEvento()
    // ---------------------------------------------------------------------

    @Test
    fun cb23_formatearFechaHoraFormatoCompleto() {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(2025, Calendar.JANUARY, 5, 8, 3, 7)
        }
        assertEquals("05/01/2025 08:03:07", Validaciones.formatearFechaHora(calendar.time))
    }

    @Test
    fun cb24_formatearFechaHoraMedianoche() {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
        }
        assertEquals("01/01/2025 00:00:00", Validaciones.formatearFechaHora(calendar.time))
    }

    @Test
    fun cb25_parsearFechaEventoFormatoValido() {
        val fecha = Validaciones.parsearFechaEvento("25/12/2025")
        assertNotNull(fecha)
        val calendar = Calendar.getInstance().apply { time = fecha!! }
        assertEquals(25, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(12, calendar.get(Calendar.MONTH) + 1)
        assertEquals(2025, calendar.get(Calendar.YEAR))
    }

    @Test
    fun cb26_parsearFechaEventoFormatoInvalidoDevuelveNull() {
        assertNull(Validaciones.parsearFechaEvento("25-12-2025"))
    }
}
