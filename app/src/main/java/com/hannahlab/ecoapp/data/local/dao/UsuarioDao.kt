package com.hannahlab.ecoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hannahlab.ecoapp.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun findByCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun findById(id: Long): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id")
    fun observeById(id: Long): Flow<Usuario?>

    @Query("SELECT * FROM usuario WHERE activo = 1 ORDER BY nombre")
    fun observeActivos(): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun count(): Int

    @Insert
    suspend fun insert(usuario: Usuario): Long

    @Update
    suspend fun update(usuario: Usuario)
}
