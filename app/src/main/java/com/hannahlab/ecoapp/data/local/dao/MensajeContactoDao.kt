package com.hannahlab.ecoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeContactoDao {
    @Query("SELECT * FROM mensaje_contacto ORDER BY enviado_en DESC")
    fun observeAll(): Flow<List<MensajeContacto>>

    @Insert
    suspend fun insert(mensaje: MensajeContacto): Long
}
