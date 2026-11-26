package com.hannahlab.ecoapp.data.local.entity

/** Datos del formulario de contacto, que se guardan en Firestore. */
data class MensajeContacto(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val asunto: String,
    val mensaje: String,
)
