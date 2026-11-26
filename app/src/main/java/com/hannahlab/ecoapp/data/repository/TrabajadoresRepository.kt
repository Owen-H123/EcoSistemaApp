package com.hannahlab.ecoapp.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.hannahlab.ecoapp.data.auth.esperar
import com.hannahlab.ecoapp.data.local.SeedData
import com.hannahlab.ecoapp.data.local.dao.UsuarioDao
import com.hannahlab.ecoapp.data.local.entity.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** El servidor no respondió a tiempo. */
class SinRespuestaException : Exception()

/**
 * Ficha de cada trabajador (cargo, tarifa y meta) en Firestore: `trabajadores/{correo}`.
 * Firestore manda y Room guarda una copia, que es la que leen las pantallas del panel.
 */
class TrabajadoresRepository(
    private val context: Context,
    private val usuarioDao: UsuarioDao,
) {
    private val fichas by lazy { FirebaseFirestore.getInstance().collection(COLECCION) }

    /** Crea la ficha propia si todavía no existe. Un trabajador siempre nace sin tarifa. */
    suspend fun asegurarFicha(usuario: Usuario) {
        val referencia = fichas.document(usuario.correo)
        if (referencia.get().esperar().exists()) return
        val propia = if (usuario.esAdmin) {
            usuario
        } else {
            usuario.copy(cargo = Usuario.CARGO_POR_DEFECTO, tarifaHora = 0.0, activo = true)
        }
        referencia.set(propia.aFicha()).esperar()
    }

    /** Deja listos los datos al abrir el panel. Si falla, la sincronización igual continúa. */
    suspend fun preparar(usuarioId: Long) {
        val usuario = usuarioDao.findById(usuarioId) ?: return
        try {
            withTimeoutOrNull(ESPERA_MS) {
                if (usuario.esAdmin) subirDemostracion()
                asegurarFicha(usuario)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudieron preparar las fichas", e)
        }
    }

    /**
     * Guarda la ficha de un trabajador (solo admin).
     * Devuelve false si quedó pendiente de enviarse por falta de conexión.
     */
    suspend fun guardar(usuario: Usuario): Boolean {
        val tarea = fichas.document(usuario.correo).set(usuario.aFicha())
        val confirmado = withTimeoutOrNull(ESPERA_MS) { tarea.esperar(); true } ?: false
        usuarioDao.update(usuario)
        return confirmado
    }

    /**
     * Alta de un trabajador (solo admin): crea su cuenta y su ficha.
     * La cuenta se crea en una segunda instancia de Firebase para no cerrar la sesión del admin.
     */
    suspend fun crear(nuevo: Usuario, contrasena: String) {
        val auth = authDeAltas()
        try {
            val cuenta = checkNotNull(auth.createUserWithEmailAndPassword(nuevo.correo, contrasena).esperar().user)
            try {
                val cambios = UserProfileChangeRequest.Builder().setDisplayName(nuevo.nombre).build()
                cuenta.updateProfile(cambios).esperar()
                withTimeoutOrNull(ESPERA_MS) { fichas.document(nuevo.correo).set(nuevo.aFicha()).esperar(); true }
                    ?: throw SinRespuestaException()
            } catch (e: Exception) {
                // Sin ficha, la cuenta se elimina para que el alta se pueda repetir
                withContext(NonCancellable) {
                    runCatching { cuenta.delete().esperar() }
                }
                throw e
            }
            guardarLocal(nuevo)
        } finally {
            auth.signOut()
        }
    }

    private fun authDeAltas(): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == APP_DE_ALTAS }
            ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, APP_DE_ALTAS)
        return FirebaseAuth.getInstance(app)
    }

    /** Sube a Firestore los trabajadores de demostración que aún no estén. */
    private suspend fun subirDemostracion() {
        SeedData.correosDeTrabajadores().forEach { correo ->
            val local = usuarioDao.findByCorreo(correo) ?: return@forEach
            val referencia = fichas.document(correo)
            // Se consulta al servidor para no pisar una ficha que esta copia local no conozca
            if (!referencia.get(Source.SERVER).esperar().exists()) referencia.set(local.aFicha()).esperar()
        }
    }

    /** Fichas en vivo. El admin recibe todo el equipo; el resto, solo la suya. */
    fun cambios(usuario: Usuario): Flow<List<Usuario>> = callbackFlow {
        val oyente = if (usuario.esAdmin) {
            fichas.addSnapshotListener { consulta, error ->
                if (error != null) close(error)
                else trySend(consulta?.documents?.mapNotNull { it.aTrabajador() }.orEmpty())
            }
        } else {
            fichas.document(usuario.correo).addSnapshotListener { documento, error ->
                if (error != null) close(error)
                else trySend(listOfNotNull(documento?.aTrabajador()))
            }
        }
        awaitClose { oyente.remove() }
    }

    /** Crea o actualiza en Room la copia de una ficha. */
    suspend fun guardarLocal(ficha: Usuario) {
        val existente = usuarioDao.findByCorreo(ficha.correo)
        if (existente == null) {
            usuarioDao.insert(ficha)
            return
        }
        // El id, el rol y la contraseña local no vienen de la ficha
        val actualizado = existente.copy(
            nombre = ficha.nombre,
            cargo = ficha.cargo,
            tarifaHora = ficha.tarifaHora,
            metaHoras = ficha.metaHoras,
            activo = ficha.activo,
        )
        if (actualizado != existente) usuarioDao.update(actualizado)
    }

    private fun Usuario.aFicha() = mapOf(
        "nombre" to nombre,
        "cargo" to cargo,
        "tarifaHora" to tarifaHora,
        "metaHoras" to metaHoras,
        "activo" to activo,
    )

    private fun DocumentSnapshot.aTrabajador(): Usuario? {
        val nombre = getString("nombre") ?: return null
        return Usuario(
            nombre = nombre,
            correo = id,
            contrasenaHash = "",
            rol = Usuario.ROL_DEVELOPER,
            cargo = getString("cargo").orEmpty(),
            tarifaHora = getDouble("tarifaHora") ?: 0.0,
            metaHoras = getDouble("metaHoras") ?: Usuario.META_POR_DEFECTO,
            activo = getBoolean("activo") ?: true,
        )
    }

    private companion object {
        const val COLECCION = "trabajadores"
        const val APP_DE_ALTAS = "altas"
        const val ESPERA_MS = 10_000L
        const val ETIQUETA = "Trabajadores"
    }
}
