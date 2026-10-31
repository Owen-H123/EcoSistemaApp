package com.hannahlab.ecoapp.data.repository

import com.hannahlab.ecoapp.data.local.dao.MensajeContactoDao
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto

interface ContactoRepository {
    suspend fun enviar(mensaje: MensajeContacto): Long
}

class LocalContactoRepository(private val dao: MensajeContactoDao) : ContactoRepository {
    override suspend fun enviar(mensaje: MensajeContacto) = dao.insert(mensaje)
}
