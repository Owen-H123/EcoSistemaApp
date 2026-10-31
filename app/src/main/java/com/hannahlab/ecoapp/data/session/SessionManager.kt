package com.hannahlab.ecoapp.data.session

import android.content.Context
import androidx.core.content.edit
import com.hannahlab.ecoapp.data.local.entity.Usuario

/** Guarda quién inició sesión y con qué rol. */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    val usuarioId: Long?
        get() = prefs.getLong(KEY_USUARIO_ID, -1L).takeIf { it > 0 }

    val esAdmin: Boolean
        get() = prefs.getString(KEY_ROL, null) == Usuario.ROL_ADMIN

    fun iniciar(usuario: Usuario) = prefs.edit {
        putLong(KEY_USUARIO_ID, usuario.id)
        putString(KEY_ROL, usuario.rol)
    }

    fun cerrar() = prefs.edit { clear() }

    private companion object {
        const val KEY_USUARIO_ID = "usuario_id"
        const val KEY_ROL = "rol"
    }
}
