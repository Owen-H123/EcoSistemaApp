package com.hannahlab.ecoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hannahlab.ecoapp.data.local.entity.HorasPorUsuario
import com.hannahlab.ecoapp.data.local.entity.RegistroDetalle
import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroHorasDao {
    /** Registros entre dos días (epochDay). Con usuarioId nulo trae los de todo el equipo. */
    @Query(
        """
        SELECT r.*, u.nombre AS nombre_usuario
        FROM registro_horas r
        INNER JOIN usuario u ON u.id = r.usuario_id
        WHERE r.fecha BETWEEN :desde AND :hasta
          AND (:usuarioId IS NULL OR r.usuario_id = :usuarioId)
        ORDER BY r.fecha DESC, r.id DESC
        """
    )
    fun observeDetalle(usuarioId: Long?, desde: Long, hasta: Long): Flow<List<RegistroDetalle>>

    @Query("SELECT COALESCE(SUM(horas), 0) FROM registro_horas WHERE usuario_id = :usuarioId AND fecha BETWEEN :desde AND :hasta")
    fun observeHorasUsuario(usuarioId: Long, desde: Long, hasta: Long): Flow<Double>

    @Query("SELECT usuario_id, SUM(horas) AS horas FROM registro_horas WHERE fecha BETWEEN :desde AND :hasta GROUP BY usuario_id")
    fun observeHorasPorUsuario(desde: Long, hasta: Long): Flow<List<HorasPorUsuario>>

    @Insert
    suspend fun insertAll(registros: List<RegistroHoras>)
}
