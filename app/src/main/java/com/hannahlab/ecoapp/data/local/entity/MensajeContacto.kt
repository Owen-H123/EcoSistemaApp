package com.hannahlab.ecoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensaje_contacto")
data class MensajeContacto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val asunto: String,
    val mensaje: String,
    @ColumnInfo(name = "enviado_en") val enviadoEn: Long = System.currentTimeMillis(),
)
