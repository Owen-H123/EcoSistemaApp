package com.hannahlab.ecoapp.data.repository

import com.hannahlab.ecoapp.data.local.dao.TrabajadorDao
import com.hannahlab.ecoapp.data.local.entity.Trabajador
import kotlinx.coroutines.flow.Flow

interface TrabajadorRepository {
    fun observeAll(): Flow<List<Trabajador>>
    suspend fun findById(id: Long): Trabajador?
    suspend fun update(trabajador: Trabajador)
}

class LocalTrabajadorRepository(private val dao: TrabajadorDao) : TrabajadorRepository {
    override fun observeAll() = dao.observeAll()
    override suspend fun findById(id: Long) = dao.findById(id)
    override suspend fun update(trabajador: Trabajador) = dao.update(trabajador)
}
