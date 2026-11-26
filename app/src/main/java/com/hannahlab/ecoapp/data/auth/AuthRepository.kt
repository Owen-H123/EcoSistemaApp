package com.hannahlab.ecoapp.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hannahlab.ecoapp.data.local.dao.UsuarioDao
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.data.session.SessionManager
import kotlinx.coroutines.withTimeoutOrNull

/** El perfil no se pudo leer ni crear en Firestore a tiempo. */
class PerfilNoDisponibleException : Exception()

/** El trabajador fue dado de baja. */
class CuentaInactivaException : Exception()

/**
 * Cuentas con Firebase Authentication y perfil en Firestore (`usuarios/{uid}`).
 * Las horas y la planilla siguen en Room, por eso cada cuenta se enlaza por correo
 * con un usuario local.
 */
class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager,
) {
    private val auth by lazy { FirebaseAuth.getInstance().apply { setLanguageCode("es") } }
    private val perfiles by lazy { FirebaseFirestore.getInstance().collection(COLECCION_USUARIOS) }

    val haySesion: Boolean
        get() = auth.currentUser != null && session.usuarioId != null

    suspend fun iniciarSesion(correo: String, contrasena: String) {
        val cuenta = auth.signInWithEmailAndPassword(correo, contrasena).esperar().user
        completarIngreso(checkNotNull(cuenta))
    }

    suspend fun registrar(nombre: String, correo: String, contrasena: String) {
        val cuenta = auth.createUserWithEmailAndPassword(correo, contrasena).esperar().user
        completarIngreso(checkNotNull(cuenta), nombre)
    }

    suspend fun enviarRecuperacion(correo: String) {
        auth.sendPasswordResetEmail(correo).esperar()
    }

    fun cerrarSesion() {
        auth.signOut()
        session.cerrar()
    }

    /** Carga el perfil y guarda la sesión. Si algo falla, no deja la cuenta a medio iniciar. */
    private suspend fun completarIngreso(cuenta: FirebaseUser, nombreNuevo: String? = null) {
        try {
            if (nombreNuevo != null) {
                // Queda en la cuenta por si el perfil no se llega a guardar ahora.
                val cambios = UserProfileChangeRequest.Builder().setDisplayName(nombreNuevo).build()
                cuenta.updateProfile(cambios).esperar()
            }
            val perfil = withTimeoutOrNull(ESPERA_PERFIL_MS) { cargarPerfil(cuenta, nombreNuevo) }
                ?: throw PerfilNoDisponibleException()
            val usuario = enlazarUsuarioLocal(perfil)
            if (!usuario.activo) throw CuentaInactivaException()
            session.iniciar(usuario)
        } catch (e: Exception) {
            cerrarSesion()
            throw e
        }
    }

    /** Lee el perfil de Firestore; la primera vez lo crea con rol de trabajador. */
    private suspend fun cargarPerfil(cuenta: FirebaseUser, nombreNuevo: String?): Perfil {
        val correo = cuenta.email.orEmpty().lowercase()
        val nombreCuenta = nombreNuevo
            ?: cuenta.displayName?.takeIf { it.isNotBlank() }
            ?: correo.substringBefore('@')
        val referencia = perfiles.document(cuenta.uid)
        val documento = referencia.get().esperar()

        if (documento.exists()) {
            return Perfil(
                nombre = documento.getString("nombre") ?: nombreCuenta,
                correo = correo,
                rol = documento.getString("rol") ?: Usuario.ROL_DEVELOPER,
            )
        }
        referencia.set(
            mapOf(
                "nombre" to nombreCuenta,
                "correo" to correo,
                "rol" to Usuario.ROL_DEVELOPER,
                "creadoEn" to FieldValue.serverTimestamp(),
            ),
        ).esperar()
        return Perfil(nombreCuenta, correo, Usuario.ROL_DEVELOPER)
    }

    private suspend fun enlazarUsuarioLocal(perfil: Perfil): Usuario {
        val existente = usuarioDao.findByCorreo(perfil.correo)
        if (existente != null) {
            val actualizado = existente.copy(nombre = perfil.nombre, rol = perfil.rol)
            usuarioDao.update(actualizado)
            return actualizado
        }
        val nuevo = Usuario(
            nombre = perfil.nombre,
            correo = perfil.correo,
            contrasenaHash = "", // La contraseña la guarda Firebase Authentication
            rol = perfil.rol,
            cargo = CARGO_POR_DEFECTO,
            tarifaHora = 0.0,
            metaHoras = META_POR_DEFECTO,
        )
        return nuevo.copy(id = usuarioDao.insert(nuevo))
    }

    private data class Perfil(val nombre: String, val correo: String, val rol: String)

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
        const val ESPERA_PERFIL_MS = 15_000L
        const val CARGO_POR_DEFECTO = "Por asignar"
        const val META_POR_DEFECTO = 160.0
    }
}
