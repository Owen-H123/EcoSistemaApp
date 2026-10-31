package com.hannahlab.ecoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registro_horas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("usuario_id"), Index("fecha")],
)
data class RegistroHoras(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "usuario_id") val usuarioId: Long,
    /** Día del registro como epochDay (LocalDate.toEpochDay). */
    val fecha: Long,
    val proyecto: String,
    val actividad: String,
    val horas: Double,
    val completada: Boolean,
)

/** Registro con el nombre de su dueño, para las vistas del admin. */
data class RegistroDetalle(
    @Embedded val registro: RegistroHoras,
    @ColumnInfo(name = "nombre_usuario") val nombreUsuario: String,
)

data class HorasPorUsuario(
    @ColumnInfo(name = "usuario_id") val usuarioId: Long,
    val horas: Double,
)
