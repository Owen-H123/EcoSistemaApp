package com.hannahlab.ecoapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hannahlab.ecoapp.data.auth.esperar
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull

interface ContactoRepository {
    /** Devuelve false si el mensaje quedó pendiente de enviarse por falta de conexión. */
    suspend fun enviar(mensaje: MensajeContacto): Boolean

    /** Cantidad de mensajes enviados por la cuenta actual. */
    fun contarEnviados(): Flow<Int>
}

/** Mensajes del formulario de contacto en la colección `mensajes` de Firestore. */
class FirestoreContactoRepository : ContactoRepository {

    private val mensajes = FirebaseFirestore.getInstance().collection("mensajes")
    private val uid: String
        get() = checkNotNull(FirebaseAuth.getInstance().currentUser).uid

    override suspend fun enviar(mensaje: MensajeContacto): Boolean {
        val tarea = mensajes.add(
            mapOf(
                "nombre" to mensaje.nombre,
                "correo" to mensaje.correo,
                "telefono" to mensaje.telefono,
                "asunto" to mensaje.asunto,
                "mensaje" to mensaje.mensaje,
                "uid" to uid,
                "creadoEn" to FieldValue.serverTimestamp(),
            ),
        )
        return withTimeoutOrNull(ESPERA_MS) { tarea.esperar(); true } ?: false
    }

    override fun contarEnviados(): Flow<Int> = callbackFlow {
        val oyente = mensajes.whereEqualTo("uid", uid).addSnapshotListener { consulta, error ->
            if (error != null) close(error) else trySend(consulta?.size() ?: 0)
        }
        awaitClose { oyente.remove() }
    }

    private companion object {
        const val ESPERA_MS = 10_000L
    }
}
