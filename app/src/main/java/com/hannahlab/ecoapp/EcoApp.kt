package com.hannahlab.ecoapp

import android.app.Application
import android.content.Context
import com.hannahlab.ecoapp.data.local.AppDatabase
import com.hannahlab.ecoapp.data.local.SeedData
import com.hannahlab.ecoapp.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EcoApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val session: SessionManager by lazy { SessionManager(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { SeedData.poblarSiVacia(database) }
    }
}

val Context.ecoApp: EcoApp get() = applicationContext as EcoApp
