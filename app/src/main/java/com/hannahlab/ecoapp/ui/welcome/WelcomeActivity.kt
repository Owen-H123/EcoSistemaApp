package com.hannahlab.ecoapp.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.databinding.ActivityWelcomeBinding
import com.hannahlab.ecoapp.ui.main.MainActivity
import com.hannahlab.ecoapp.ui.recuperar.RecuperarContrasenaActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener { openMain(openLogin = false) }
        binding.btnIniciarSesion.setOnClickListener { openMain(openLogin = true) }
        binding.tvOlvide.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasenaActivity::class.java))
        }
    }

    private fun openMain(openLogin: Boolean) {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_LOGIN, openLogin)
        )
        finish()
    }
}
