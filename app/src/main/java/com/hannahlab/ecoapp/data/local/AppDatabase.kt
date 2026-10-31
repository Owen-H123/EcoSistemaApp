package com.hannahlab.ecoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hannahlab.ecoapp.data.local.dao.MensajeContactoDao
import com.hannahlab.ecoapp.data.local.dao.TrabajadorDao
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto
import com.hannahlab.ecoapp.data.local.entity.Trabajador

@Database(entities = [Trabajador::class, MensajeContacto::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trabajadorDao(): TrabajadorDao
    abstract fun mensajeContactoDao(): MensajeContactoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ecoapp.db")
                .build()
                .also { instance = it }
        }
    }
}
