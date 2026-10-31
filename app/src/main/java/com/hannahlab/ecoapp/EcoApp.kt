package com.hannahlab.ecoapp

import android.app.Application
import com.hannahlab.ecoapp.data.local.AppDatabase

class EcoApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
