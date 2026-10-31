package com.hannahlab.ecoapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.databinding.ActivitySplashBinding
import com.hannahlab.ecoapp.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val goToMain = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(goToMain, SPLASH_DURATION_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(goToMain)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 5_000L
    }
}
