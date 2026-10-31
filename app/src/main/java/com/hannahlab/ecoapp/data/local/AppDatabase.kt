package com.hannahlab.ecoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hannahlab.ecoapp.data.local.dao.MensajeContactoDao
import com.hannahlab.ecoapp.data.local.dao.RegistroHorasDao
import com.hannahlab.ecoapp.data.local.dao.UsuarioDao
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto
import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import com.hannahlab.ecoapp.data.local.entity.Usuario

@Database(
    entities = [Usuario::class, RegistroHoras::class, MensajeContacto::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroHorasDao(): RegistroHorasDao
    abstract fun mensajeContactoDao(): MensajeContactoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ecoapp.db")
                // App de curso con datos semilla: si cambia el esquema se recrea la base
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}
