package com.hannahlab.ecoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hannahlab.ecoapp.data.local.entity.Trabajador
import kotlinx.coroutines.flow.Flow

@Dao
interface TrabajadorDao {
    @Query("SELECT * FROM trabajador ORDER BY nombre")
    fun observeAll(): Flow<List<Trabajador>>

    @Query("SELECT * FROM trabajador WHERE id = :id")
    suspend fun findById(id: Long): Trabajador?

    @Insert
    suspend fun insertAll(trabajadores: List<Trabajador>)

    @Update
    suspend fun update(trabajador: Trabajador)
}
