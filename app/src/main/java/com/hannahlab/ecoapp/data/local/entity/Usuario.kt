package com.hannahlab.ecoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuario", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    @ColumnInfo(name = "contrasena_hash") val contrasenaHash: String,
    val rol: String,
    val cargo: String,
    @ColumnInfo(name = "tarifa_hora") val tarifaHora: Double,
    @ColumnInfo(name = "meta_horas") val metaHoras: Double,
    val activo: Boolean = true,
) {
    val esAdmin: Boolean get() = rol == ROL_ADMIN

    companion object {
        const val ROL_ADMIN = "ADMIN"
        const val ROL_DEVELOPER = "DEVELOPER"
    }
}
