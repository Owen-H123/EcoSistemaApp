package com.hannahlab.ecoapp.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.hannahlab.ecoapp.data.auth.esperar
import com.hannahlab.ecoapp.data.local.SeedData
import com.hannahlab.ecoapp.data.local.dao.RegistroHorasDao
import com.hannahlab.ecoapp.data.local.dao.UsuarioDao
import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import com.hannahlab.ecoapp.data.local.entity.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Registros de horas en la colección `registros` de Firestore, con una copia en Room
 * que es la que leen las pantallas del panel.
 */
class RegistrosRepository(
    private val usuarioDao: UsuarioDao,
    private val registroDao: RegistroHorasDao,
    private val trabajadores: TrabajadoresRepository,
) {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val registros by lazy { firestore.collection(COLECCION) }

    /** Deja listos los datos al abrir el panel. Si falla, la sincronización igual continúa. */
    suspend fun preparar(usuarioId: Long) {
        trabajadores.preparar(usuarioId)
        if (usuarioDao.findById(usuarioId)?.esAdmin != true) return
        try {
            withTimeoutOrNull(ESPERA_DEMOSTRACION_MS) { subirDemostracion() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudieron subir las horas de demostración", e)
        }
    }

    /**
     * Copia en Room las fichas y las horas mientras se observe.
     * El admin recibe las de todo el equipo; el resto, solo las suyas.
     */
    suspend fun sincronizar(usuarioId: Long) {
        val usuario = usuarioDao.findById(usuarioId) ?: return
        combine(trabajadores.cambios(usuario), cambios(usuario)) { fichas, horas -> fichas to horas }
            .collect { (fichas, horas) ->
                // Las fichas van primero: cada registro necesita que exista su trabajador
                fichas.forEach { trabajadores.guardarLocal(it) }
                val ids = horas.map { it.correo }.distinct()
                    .associateWith { usuarioDao.findByCorreo(it)?.id }
                val locales = horas.mapNotNull { remoto ->
                    ids[remoto.correo]?.let { remoto.registro.copy(usuarioId = it) }
                }
                registroDao.reemplazar(if (usuario.esAdmin) null else usuario.id, locales)
            }
    }

    /** Devuelve false si el registro quedó pendiente de enviarse por falta de conexión. */
    suspend fun registrar(usuario: Usuario, registro: RegistroHoras): Boolean {
        val tarea = registros.add(datos(usuario.correo, registro))
        val confirmado = withTimeoutOrNull(ESPERA_MS) { tarea.esperar(); true } ?: false
        registroDao.insertAll(listOf(registro.copy(usuarioId = usuario.id)))
        return confirmado
    }

    private fun cambios(usuario: Usuario): Flow<List<RegistroRemoto>> = callbackFlow {
        val consulta: Query =
            if (usuario.esAdmin) registros else registros.whereEqualTo("correo", usuario.correo)
        // La copia local se reemplaza entera, así que solo sirven respuestas confirmadas por el servidor
        val oyente = consulta.addSnapshotListener(MetadataChanges.INCLUDE) { resultado, error ->
            if (error != null) {
                close(error)
            } else if (resultado != null && !resultado.metadata.isFromCache) {
                val ordenados = resultado.documents
                    // Un registro recién creado aún no tiene fecha del servidor: va al final
                    .sortedBy { it.getTimestamp("creadoEn")?.seconds ?: Long.MAX_VALUE }
                    .mapNotNull { it.aRegistro() }
                trySend(ordenados)
            }
        }
        awaitClose { oyente.remove() }
    }

    /** Sube las horas de ejemplo de cada trabajador de demostración que todavía no tenga ninguna. */
    private suspend fun subirDemostracion() {
        SeedData.correosDeTrabajadores().forEach { correo ->
            val existentes = registros.whereEqualTo("correo", correo).limit(1).get(Source.SERVER).esperar()
            if (!existentes.isEmpty) return@forEach
            SeedData.registrosDeDemostracion(correo)
                .withIndex()
                .chunked(TAMANO_LOTE)
                .forEach { grupo ->
                    val lote = firestore.batch()
                    grupo.forEach { (indice, registro) ->
                        lote.set(registros.document("demo-$correo-$indice"), datos(correo, registro))
                    }
                    lote.commit().esperar()
                }
        }
    }

    private fun datos(correo: String, registro: RegistroHoras) = mapOf(
        "correo" to correo,
        "fecha" to registro.fecha,
        "proyecto" to registro.proyecto,
        "actividad" to registro.actividad,
        "horas" to registro.horas,
        "completada" to registro.completada,
        "creadoEn" to FieldValue.serverTimestamp(),
    )

    private fun DocumentSnapshot.aRegistro(): RegistroRemoto? {
        val correo = getString("correo") ?: return null
        val registro = RegistroHoras(
            usuarioId = 0, // Se completa con el id local del trabajador
            fecha = getLong("fecha") ?: return null,
            proyecto = getString("proyecto").orEmpty(),
            actividad = getString("actividad").orEmpty(),
            horas = getDouble("horas") ?: return null,
            completada = getBoolean("completada") ?: false,
        )
        return RegistroRemoto(correo, registro)
    }

    private data class RegistroRemoto(val correo: String, val registro: RegistroHoras)

    private companion object {
        const val COLECCION = "registros"
        const val ESPERA_MS = 10_000L
        const val ESPERA_DEMOSTRACION_MS = 20_000L
        const val TAMANO_LOTE = 20
        const val ETIQUETA = "Registros"
    }
}
