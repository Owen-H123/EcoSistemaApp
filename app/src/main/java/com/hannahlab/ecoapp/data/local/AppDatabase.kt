package com.hannahlab.ecoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hannahlab.ecoapp.data.local.dao.RegistroHorasDao
import com.hannahlab.ecoapp.data.local.dao.UsuarioDao
import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import com.hannahlab.ecoapp.data.local.entity.Usuario

/** Copia local de las fichas y las horas de Firestore. */
@Database(
    entities = [Usuario::class, RegistroHoras::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroHorasDao(): RegistroHorasDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** Los mensajes de contacto pasaron a Firestore. Se migra para no perder la sesión guardada. */
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS mensaje_contacto")
            }
        }

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ecoapp.db")
                .addMigrations(MIGRACION_2_3)
                // App de curso con datos semilla: ante cualquier otro cambio de esquema se recrea la base
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}
