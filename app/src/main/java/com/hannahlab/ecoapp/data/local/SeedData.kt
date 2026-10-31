package com.hannahlab.ecoapp.data.local

import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.util.Seguridad
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

/**
 * Datos de demostración. Personas, tarifas, proyectos y horas son FICTICIOS:
 * no corresponden a trabajadores ni clientes reales de Hannah Lab.
 */
object SeedData {

    suspend fun poblarSiVacia(db: AppDatabase) {
        val usuarioDao = db.usuarioDao()
        if (usuarioDao.count() > 0) return

        val registros = usuarios().flatMap { usuario ->
            val id = usuarioDao.insert(usuario)
            if (usuario.esAdmin) emptyList() else generarRegistros(id)
        }
        db.registroHorasDao().insertAll(registros)
    }

    private fun usuarios() = listOf(
        Usuario(
            nombre = "Admin Hannah", correo = "admin@ecoapp.test",
            contrasenaHash = Seguridad.hash("admin123"), rol = Usuario.ROL_ADMIN,
            cargo = "Líder técnico", tarifaHora = 35.0, metaHoras = 160.0,
        ),
        Usuario(
            nombre = "Ana Torres", correo = "ana@ecoapp.test",
            contrasenaHash = Seguridad.hash("dev123"), rol = Usuario.ROL_DEVELOPER,
            cargo = "Desarrolladora frontend", tarifaHora = 25.0, metaHoras = 160.0,
        ),
        Usuario(
            nombre = "Luis Ramos", correo = "luis@ecoapp.test",
            contrasenaHash = Seguridad.hash("dev123"), rol = Usuario.ROL_DEVELOPER,
            cargo = "Desarrollador backend", tarifaHora = 28.0, metaHoras = 160.0,
        ),
        Usuario(
            nombre = "Carla Vega", correo = "carla@ecoapp.test",
            contrasenaHash = Seguridad.hash("dev123"), rol = Usuario.ROL_DEVELOPER,
            cargo = "Diseñadora UX/UI", tarifaHora = 22.0, metaHoras = 120.0,
        ),
    )

    private val proyectos = mapOf(
        "Portal de clientes" to listOf("Maquetar pantalla de inicio", "Integrar login", "Corregir estilos responsive"),
        "Tablero de ventas" to listOf("Diseñar gráfico de ventas", "Endpoint de métricas", "Revisar filtros"),
        "Migración de datos" to listOf("Limpiar datos de prueba", "Script de importación", "Validar totales"),
        "App EcoApp" to listOf("Pantalla de planilla", "Pruebas en emulador", "Revisión de diseño"),
    )

    /** Uno o dos registros por día hábil desde hace ~40 días hasta hoy. */
    private fun generarRegistros(usuarioId: Long): List<RegistroHoras> {
        val random = Random(usuarioId)
        val hoy = LocalDate.now()
        return generateSequence(hoy.minusDays(40)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(hoy) }
            .filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY }
            .flatMap { fecha ->
                (1..random.nextInt(1, 3)).map {
                    val (proyecto, actividades) = proyectos.entries.random(random).toPair()
                    RegistroHoras(
                        usuarioId = usuarioId,
                        fecha = fecha.toEpochDay(),
                        proyecto = proyecto,
                        actividad = actividades.random(random),
                        horas = random.nextInt(4, 11) / 2.0,
                        completada = fecha.isBefore(hoy.minusDays(2)) || random.nextBoolean(),
                    )
                }.asSequence()
            }
            .toList()
    }
}
