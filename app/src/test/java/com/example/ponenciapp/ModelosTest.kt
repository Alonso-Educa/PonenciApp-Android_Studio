package com.example.ponenciapp

import com.example.ponenciapp.data.bbdd.entities.EventoData
import com.example.ponenciapp.data.bbdd.entities.UsuarioData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

/**
 * Tests unitarios de caja blanca para los métodos `copy()` de las data classes.
 *
 * GRUPO 8 — Modelos copy()
 *   CB27  EventoData.copy(nombre="Evento Nuevo") cambia solo el nombre
 *   CB28  UsuarioData.copy() sin parámetros devuelve una instancia equivalente
 */
class ModelosTest {

    @Test
    fun cb27_eventoDataCopyCambiaSoloNombre() {
        val original = EventoData(
            idEvento = "evt-001",
            nombre = "Evento Original",
            fecha = "25/12/2025",
            lugar = "Salón de actos",
            descripcion = "Descripción del evento original",
            codigoEvento = "FORM-Ab1Z",
            idOrganizador = "org-123"
        )

        val copia = original.copy(nombre = "Evento Nuevo")

        assertEquals("Evento Nuevo", copia.nombre)
        assertEquals(original.idEvento, copia.idEvento)
        assertEquals(original.fecha, copia.fecha)
        assertEquals(original.lugar, copia.lugar)
        assertEquals(original.descripcion, copia.descripcion)
        assertEquals(original.codigoEvento, copia.codigoEvento)
        assertEquals(original.idOrganizador, copia.idOrganizador)
        assertNotSame(original, copia)
    }

    @Test
    fun cb28_usuarioDataCopySinParametrosDevuelveInstanciaEquivalente() {
        val original = UsuarioData(
            idUsuario = "usr-001",
            nombre = "Ana",
            apellidos = "García Pérez",
            emailEduca = "ana.garcia@educa.junta.es",
            centro = "IES Ejemplo",
            codigoCentro = "47000000",
            rol = "organizador",
            fechaRegistro = "01/01/2025",
            idEvento = "evt-001",
            fotoPerfilUrl = "https://example.com/foto.jpg"
        )

        val copia = original.copy()

        assertEquals(original, copia)
        assertEquals(original.idUsuario, copia.idUsuario)
        assertEquals(original.nombre, copia.nombre)
        assertEquals(original.apellidos, copia.apellidos)
        assertEquals(original.emailEduca, copia.emailEduca)
        assertEquals(original.centro, copia.centro)
        assertEquals(original.codigoCentro, copia.codigoCentro)
        assertEquals(original.rol, copia.rol)
        assertEquals(original.fechaRegistro, copia.fechaRegistro)
        assertEquals(original.idEvento, copia.idEvento)
        assertEquals(original.fotoPerfilUrl, copia.fotoPerfilUrl)
        assertNotSame(original, copia)
    }
}
