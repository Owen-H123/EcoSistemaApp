package com.hannahlab.ecoapp.util

import java.security.MessageDigest

object Seguridad {
    /** SHA-256 en hexadecimal; la contraseña nunca se guarda en texto plano. */
    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
