package com.hannahlab.ecoapp.ui.acceso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.auth.CuentaInactivaException
import com.hannahlab.ecoapp.data.auth.PerfilNoDisponibleException

/** Abre una pantalla borrando las anteriores, para que "atrás" no regrese al login ni a una sesión cerrada. */
fun Context.abrirComoInicio(destino: Class<out Activity>) {
    startActivity(Intent(this, destino).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

/** Traduce un error de Firebase al aviso que ve el usuario. */
@StringRes
fun mensajeDeError(error: Exception): Int {
    Log.w("Acceso", "Error de acceso", error)
    return when (error) {
        // Va antes que las credenciales inválidas porque hereda de ellas
        is FirebaseAuthWeakPasswordException -> R.string.error_contrasena_corta
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> R.string.login_error
        is FirebaseAuthUserCollisionException -> R.string.error_correo_duplicado
        is FirebaseNetworkException -> R.string.error_conexion
        is FirebaseTooManyRequestsException -> R.string.error_intentos
        is PerfilNoDisponibleException,
        is FirebaseFirestoreException -> R.string.error_perfil
        is CuentaInactivaException -> R.string.error_cuenta_inactiva
        else -> R.string.error_general
    }
}
