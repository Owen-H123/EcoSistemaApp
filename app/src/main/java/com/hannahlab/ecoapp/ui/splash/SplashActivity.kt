package com.hannahlab.ecoapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.databinding.ActivitySplashBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.LoginActivity
import com.hannahlab.ecoapp.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val continuar = Runnable {
        // Con sesión iniciada entra directo a la app; si no, pide iniciar sesión
        val destino = if (ecoApp.auth.haySesion) {
            MainActivity::class.java
        } else {
            ecoApp.auth.cerrarSesion() // descarta restos de una sesión incompleta
            LoginActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(continuar, SPLASH_DURATION_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(continuar)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 5_000L
    }
}
