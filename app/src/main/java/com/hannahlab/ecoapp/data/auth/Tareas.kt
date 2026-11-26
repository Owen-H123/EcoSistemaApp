package com.hannahlab.ecoapp.data.auth

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Espera el resultado de una tarea de Firebase sin bloquear el hilo. */
suspend fun <T> Task<T>.esperar(): T = suspendCancellableCoroutine { continuacion ->
    addOnCompleteListener { tarea ->
        if (tarea.isSuccessful) {
            continuacion.resume(tarea.result)
        } else {
            continuacion.resumeWithException(tarea.exception ?: IllegalStateException("Tarea cancelada"))
        }
    }
}
