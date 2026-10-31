package com.hannahlab.ecoapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.databinding.ActivitySplashBinding
import com.hannahlab.ecoapp.ui.welcome.WelcomeActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val goToWelcome = Runnable {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(goToWelcome, SPLASH_DURATION_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(goToWelcome)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 5_000L
    }
}
