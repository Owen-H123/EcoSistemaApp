package com.hannahlab.ecoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trabajador")
data class Trabajador(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cargo: String,
    @ColumnInfo(name = "tarifa_hora") val tarifaHora: Double,
    @ColumnInfo(name = "horas_mes") val horasMes: Double,
)
